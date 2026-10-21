package co.ferroforja.bodega.almacen.service;

import co.ferroforja.bodega.catalogo.entity.Ficha;
import co.ferroforja.bodega.catalogo.repository.FichaRepository;
import co.ferroforja.bodega.almacen.dto.request.AlmacenAperturaRequest;
import co.ferroforja.bodega.almacen.dto.response.AlmacenAsientoResponse;
import co.ferroforja.bodega.almacen.dto.response.AlmacenNivelResponse;
import co.ferroforja.bodega.almacen.entity.AlmacenAsiento;
import co.ferroforja.bodega.almacen.entity.AlmacenNivel;
import co.ferroforja.bodega.almacen.repository.AlmacenAsientoRepository;
import co.ferroforja.bodega.almacen.repository.AlmacenNivelRepository;
import co.ferroforja.bodega.comun.bitacora.BitacoraService;
import co.ferroforja.bodega.comun.exception.BusinessException;
import co.ferroforja.bodega.comun.exception.ExistenciaInsuficienteException;
import co.ferroforja.bodega.comun.exception.ResourceNotFoundException;
import co.ferroforja.bodega.operarios.entity.Operario;
import co.ferroforja.bodega.operarios.repository.OperarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Núcleo de Gestión de Inventario:
 * Implementa el control transaccional de existencias, bloqueo pesimista contra stock negativo (RN-01),
 * cálculo del Costo Promedio Ponderado (RN-04) y registro inmutable en Kárdex.
 */
@Service
public class AlmacenService {

    private final AlmacenNivelRepository nivelRepository;
    private final AlmacenAsientoRepository asientoRepository;
    private final FichaRepository fichaRepository;
    private final OperarioRepository operarioRepository;
    private final BitacoraService bitacoraService;

    public AlmacenService(AlmacenNivelRepository nivelRepository,
                            AlmacenAsientoRepository asientoRepository,
                            FichaRepository fichaRepository,
                            OperarioRepository operarioRepository,
                            BitacoraService bitacoraService) {
        this.nivelRepository = nivelRepository;
        this.asientoRepository = asientoRepository;
        this.fichaRepository = fichaRepository;
        this.operarioRepository = operarioRepository;
        this.bitacoraService = bitacoraService;
    }

    @Transactional(readOnly = true)
    public Page<AlmacenNivelResponse> enumerarNiveles(String search, boolean existenciaBaja, Pageable pageable) {
        return nivelRepository.searchNiveles(search, existenciaBaja, pageable).map(this::mapNivelToResponse);
    }

    @Transactional(readOnly = true)
    public Page<AlmacenAsientoResponse> enumerarAsientosPorFicha(Long fichaId, Pageable pageable) {
        return asientoRepository.findByFichaIdOrderByMarcaTiempoDesc(fichaId, pageable)
                .map(this::mapAsientoToResponse);
    }

    /**
     * Carga de inventario inicial (RN-04, RN-08).
     * Solo para referencias con saldo en cero y sin movimientos previos.
     */
    @Transactional
    public AlmacenNivelResponse inicializarAlmacen(AlmacenAperturaRequest request, String username) {
        Ficha fch = fichaRepository.findById(request.getFichaId())
                .orElseThrow(() -> new ResourceNotFoundException("Referencia no encontrada con ID: " + request.getFichaId()));

        if (asientoRepository.existsByFichaId(fch.getId())) {
            throw new BusinessException("La referencia ya registra movimientos en el sistema. Utilice un ajuste o compra.");
        }

        AlmacenNivel nivel = nivelRepository.findByFichaIdForUpdate(fch.getId())
                .orElseGet(() -> new AlmacenNivel(fch, BigDecimal.ZERO, BigDecimal.ZERO));

        nivel.setVolumenLibre(request.getVolumenApertura());
        nivel.setCosteMedioPonderado(request.getCosteIndividual().setScale(4, RoundingMode.HALF_UP));
        AlmacenNivel nivelGuardado = nivelRepository.save(nivel);

        Operario operario = operarioRepository.findByUsername(username).orElse(null);

        // Registrar movimiento Kárdex inicial
        asentarAsientoInterno(
                fch,
                "INICIALIZACION",
                request.getVolumenApertura(),
                request.getCosteIndividual(),
                nivel.getVolumenLibre(),
                "REAJUSTE",
                0L,
                "Inventario inicial de arranque",
                operario
        );

        bitacoraService.asentarEvento(operario, "ALMACEN", "ALMACEN_APERTURA",
                "Carga inicial de " + request.getVolumenApertura() + " unidades a costo $" + request.getCosteIndividual() + " para " + fch.getCodigoEan());

        return mapNivelToResponse(nivelGuardado);
    }

    /**
     * Descuento atómico de existencias con Bloqueo Pesimista (RN-01).
     * Retorna el costo histórico unitario que tenía el producto al momento del descuento.
     */
    @Transactional
    public BigDecimal restarExistenciaConCerrojo(Long fichaId, BigDecimal volumen, String docTipo, Long docId, String causa, Operario operario) {
        AlmacenNivel nivel = nivelRepository.findByFichaIdForUpdate(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Saldo no encontrado para la referencia ID: " + fichaId));

        if (nivel.getVolumenLibre().compareTo(volumen) < 0) {
            throw new ExistenciaInsuficienteException("Stock insuficiente para " + nivel.getFicha().getCodigoEan() +
                    " (" + nivel.getFicha().getNombreVisible() + "). Disponibles: " +
                    nivel.getVolumenLibre() + ", Solicitadas: " + volumen);
        }

        BigDecimal nuevoNivel = nivel.getVolumenLibre().subtract(volumen);
        nivel.setVolumenLibre(nuevoNivel);
        nivelRepository.save(nivel);

        BigDecimal costeCongelado = nivel.getCosteMedioPonderado();

        asentarAsientoInterno(
                nivel.getFicha(),
                "EXPENDIO",
                volumen,
                costeCongelado,
                nuevoNivel,
                docTipo,
                docId,
                causa,
                operario
        );

        return costeCongelado;
    }

    /**
     * Incremento de existencias por compra y recálculo del Costo Promedio Ponderado (RN-03, RN-04).
     */
    @Transactional
    public void tramitarEntradaAbastecimiento(Long fichaId, BigDecimal volumen, BigDecimal costeIndividual, Long abastecimientoId, Operario operario) {
        AlmacenNivel nivel = nivelRepository.findByFichaIdForUpdate(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Saldo no encontrado para la referencia ID: " + fichaId));

        BigDecimal existenciaActual = nivel.getVolumenLibre();
        BigDecimal cppActual = nivel.getCosteMedioPonderado();

        // Fórmula de Costo Promedio Ponderado:
        // Nuevo CPP = [(ExistenciaActual * CPPActual) + (VolumenComprada * CosteIndividualAbastecimiento)] / (ExistenciaActual + VolumenComprada)
        BigDecimal valorAlmacenActual = existenciaActual.multiply(cppActual);
        BigDecimal valorAbastecimientoNueva = volumen.multiply(costeIndividual);
        BigDecimal nuevaExistencia = existenciaActual.add(volumen);

        BigDecimal nuevoCpp;
        if (nuevaExistencia.compareTo(BigDecimal.ZERO) > 0) {
            nuevoCpp = valorAlmacenActual.add(valorAbastecimientoNueva).divide(nuevaExistencia, 4, RoundingMode.HALF_UP);
        } else {
            nuevoCpp = costeIndividual.setScale(4, RoundingMode.HALF_UP);
        }

        nivel.setVolumenLibre(nuevaExistencia);
        nivel.setCosteMedioPonderado(nuevoCpp);
        nivelRepository.save(nivel);

        asentarAsientoInterno(
                nivel.getFicha(),
                "ABASTECIMIENTO",
                volumen,
                costeIndividual,
                nuevaExistencia,
                "ABASTECIMIENTO",
                abastecimientoId,
                "Recepción de compra a proveedor",
                operario
        );
    }

    /**
     * Reintegro de existencias por devolución o anulación de venta (RN-07).
     */
    @Transactional
    public void reponerExistencia(Long fichaId, BigDecimal volumen, BigDecimal costeAplicado, String docTipo, Long docId, String causa, Operario operario) {
        AlmacenNivel nivel = nivelRepository.findByFichaIdForUpdate(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Saldo no encontrado para la referencia ID: " + fichaId));

        BigDecimal nuevaExistencia = nivel.getVolumenLibre().add(volumen);
        nivel.setVolumenLibre(nuevaExistencia);
        nivelRepository.save(nivel);

        asentarAsientoInterno(
                nivel.getFicha(),
                "RESTITUCION_COMPRADOR",
                volumen,
                costeAplicado,
                nuevaExistencia,
                docTipo,
                docId,
                causa,
                operario
        );
    }

    /**
     * Compensación de salida de existencias por anulación de compra.
     */
    @Transactional
    public void compensarSalidaAbastecimiento(Long fichaId, BigDecimal volumen, BigDecimal costeIndividual, Long abastecimientoId, String causa, Operario operario) {
        AlmacenNivel nivel = nivelRepository.findByFichaIdForUpdate(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Saldo no encontrado para la referencia ID: " + fichaId));

        if (nivel.getVolumenLibre().compareTo(volumen) < 0) {
            throw new ExistenciaInsuficienteException("No se puede anular la compra. Parte del stock ya fue vendido o consumido. Stock actual: " +
                    nivel.getVolumenLibre() + ", Requerido para reversión: " + volumen);
        }

        BigDecimal nuevaExistencia = nivel.getVolumenLibre().subtract(volumen);
        nivel.setVolumenLibre(nuevaExistencia);
        nivelRepository.save(nivel);

        asentarAsientoInterno(
                nivel.getFicha(),
                "ANULACION_ABASTECIMIENTO",
                volumen,
                costeIndividual,
                nuevaExistencia,
                "ABASTECIMIENTO",
                abastecimientoId,
                causa,
                operario
        );
    }

    /**
     * Compensación de entrada de existencias por anulación de venta.
     */
    @Transactional
    public void compensarEntradaExpendio(Long fichaId, BigDecimal volumen, BigDecimal costeCongelado, Long expendioId, String causa, Operario operario) {
        AlmacenNivel nivel = nivelRepository.findByFichaIdForUpdate(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Saldo no encontrado para la referencia ID: " + fichaId));

        BigDecimal nuevaExistencia = nivel.getVolumenLibre().add(volumen);
        nivel.setVolumenLibre(nuevaExistencia);
        nivelRepository.save(nivel);

        asentarAsientoInterno(
                nivel.getFicha(),
                "ANULACION_EXPENDIO",
                volumen,
                costeCongelado,
                nuevaExistencia,
                "EXPENDIO",
                expendioId,
                causa,
                operario
        );
    }

    /**
     * Ajuste manual de inventario autorizado (RN-08).
     */
    @Transactional
    public void tramitarReajusteManual(Long fichaId, String tipoReajuste, BigDecimal volumen, String causa, Long reajusteDocId, Operario operario) {
        AlmacenNivel nivel = nivelRepository.findByFichaIdForUpdate(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Saldo no encontrado para la referencia ID: " + fichaId));

        BigDecimal nuevaExistencia;
        String tipoAsi;

        if ("INCREMENTO".equalsIgnoreCase(tipoReajuste)) {
            nuevaExistencia = nivel.getVolumenLibre().add(volumen);
            tipoAsi = "REAJUSTE_ENTRADA";
        } else {
            if (nivel.getVolumenLibre().compareTo(volumen) < 0) {
                throw new ExistenciaInsuficienteException("El ajuste de disminución supera el saldo actual disponible (" + nivel.getVolumenLibre() + ")");
            }
            nuevaExistencia = nivel.getVolumenLibre().subtract(volumen);
            tipoAsi = "REAJUSTE_SALIDA";
        }

        nivel.setVolumenLibre(nuevaExistencia);
        nivelRepository.save(nivel);

        asentarAsientoInterno(
                nivel.getFicha(),
                tipoAsi,
                volumen,
                nivel.getCosteMedioPonderado(),
                nuevaExistencia,
                "REAJUSTE",
                reajusteDocId,
                causa,
                operario
        );
    }

    @Transactional(readOnly = true)
    public BigDecimal recuperarCosteMedioVigente(Long fichaId) {
        return nivelRepository.findByFichaId(fichaId)
                .map(AlmacenNivel::getCosteMedioPonderado)
                .orElse(BigDecimal.ZERO);
    }

    private void asentarAsientoInterno(Ficha fch, String tipoAsi, BigDecimal vol, BigDecimal coste,
                                           BigDecimal nivelRes, String docTipo, Long docId, String causa, Operario operario) {
        AlmacenAsiento asi = new AlmacenAsiento();
        asi.setFicha(fch);
        asi.setTipoAsiento(tipoAsi);
        asi.setVolumen(vol);
        asi.setCosteIndividualAsiento(coste.setScale(4, RoundingMode.HALF_UP));
        asi.setNivelObtenido(nivelRes);
        asi.setFolioTipo(docTipo);
        asi.setFolioId(docId);
        asi.setCausa(causa);
        asi.setMarcaTiempo(LocalDateTime.now());
        asi.setOperario(operario);

        asientoRepository.save(asi);
    }

    private AlmacenNivelResponse mapNivelToResponse(AlmacenNivel s) {
        AlmacenNivelResponse res = new AlmacenNivelResponse();
        res.setId(s.getId());
        res.setFichaId(s.getFicha().getId());
        res.setCodigoEan(s.getFicha().getCodigoEan());
        res.setArticuloDenominacion(s.getFicha().getArticulo().getDenominacion());
        res.setFabricanteDenominacion(s.getFicha().getFabricante().getDenominacion());
        res.setEmpaqueDenominacion(s.getFicha().getEmpaque().getDenominacion());
        res.setMedidaBase(s.getFicha().getMedidaBase().getClave());
        res.setVolumenLibre(s.getVolumenLibre());
        res.setCosteMedioPonderado(s.getCosteMedioPonderado());
        res.setActualizadoEn(s.getActualizadoEn());
        return res;
    }

    private AlmacenAsientoResponse mapAsientoToResponse(AlmacenAsiento m) {
        AlmacenAsientoResponse res = new AlmacenAsientoResponse();
        res.setId(m.getId());
        res.setFichaId(m.getFicha().getId());
        res.setArticuloDenominacion(m.getFicha().getNombreVisible());
        res.setTipoAsiento(m.getTipoAsiento());
        res.setVolumen(m.getVolumen());
        res.setCosteIndividualAsiento(m.getCosteIndividualAsiento());
        res.setNivelObtenido(m.getNivelObtenido());
        res.setFolioTipo(m.getFolioTipo());
        res.setFolioId(m.getFolioId());
        res.setCausa(m.getCausa());
        res.setMarcaTiempo(m.getMarcaTiempo());
        res.setOperarioDenominacion(m.getOperario() != null ? m.getOperario().getNombreVisible() : "SISTEMA");
        return res;
    }
}
