package co.ferroforja.bodega.abastecimiento.service;

import co.ferroforja.bodega.catalogo.entity.Ficha;
import co.ferroforja.bodega.catalogo.service.CatalogoService;
import co.ferroforja.bodega.socios.entity.Socio;
import co.ferroforja.bodega.socios.service.SocioService;
import co.ferroforja.bodega.folios.service.FolioSecuenciaService;
import co.ferroforja.bodega.almacen.service.AlmacenService;
import co.ferroforja.bodega.abastecimiento.dto.request.AnularFolioRequest;
import co.ferroforja.bodega.abastecimiento.dto.request.AbastecimientoDetalleRequest;
import co.ferroforja.bodega.abastecimiento.dto.request.GenerarAbastecimientoRequest;
import co.ferroforja.bodega.abastecimiento.dto.response.AbastecimientoDetalleResponse;
import co.ferroforja.bodega.abastecimiento.dto.response.AbastecimientoResponse;
import co.ferroforja.bodega.abastecimiento.entity.Abastecimiento;
import co.ferroforja.bodega.abastecimiento.entity.AbastecimientoDetalle;
import co.ferroforja.bodega.abastecimiento.repository.AbastecimientoRepository;
import co.ferroforja.bodega.comun.bitacora.BitacoraService;
import co.ferroforja.bodega.comun.exception.BusinessException;
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

@Service
public class AbastecimientoService {

    private final AbastecimientoRepository abastecimientoRepository;
    private final SocioService socioService;
    private final CatalogoService catalogoService;
    private final AlmacenService almacenService;
    private final FolioSecuenciaService secuenciaService;
    private final OperarioRepository operarioRepository;
    private final BitacoraService bitacoraService;

    public AbastecimientoService(AbastecimientoRepository abastecimientoRepository,
                           SocioService socioService,
                           CatalogoService catalogoService,
                           AlmacenService almacenService,
                           FolioSecuenciaService secuenciaService,
                           OperarioRepository operarioRepository,
                           BitacoraService bitacoraService) {
        this.abastecimientoRepository = abastecimientoRepository;
        this.socioService = socioService;
        this.catalogoService = catalogoService;
        this.almacenService = almacenService;
        this.secuenciaService = secuenciaService;
        this.operarioRepository = operarioRepository;
        this.bitacoraService = bitacoraService;
    }

    @Transactional(readOnly = true)
    public Page<AbastecimientoResponse> enumerarAbastecimientos(String situacion, Long suministradorId, LocalDateTime desde, LocalDateTime hasta, Pageable pageable) {
        return abastecimientoRepository.searchAbastecimientos(situacion, suministradorId, desde, hasta, pageable).map(this::convertirARespuesta);
    }

    @Transactional(readOnly = true)
    public AbastecimientoResponse recuperarPorId(Long id) {
        Abastecimiento abastecimiento = abastecimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con ID: " + id));
        return convertirARespuesta(abastecimiento);
    }

    @Transactional(readOnly = true)
    public AbastecimientoResponse recuperarPorCorrelativo(String correlativo) {
        Abastecimiento abastecimiento = abastecimientoRepository.findByCorrelativo(correlativo)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con consecutivo: " + correlativo));
        return convertirARespuesta(abastecimiento);
    }

    /**
     * Registro y recepción completa de compra en una sola operación transaccional (RN-03, RN-04, RN-10).
     */
    @Transactional
    public AbastecimientoResponse asentarAbastecimiento(GenerarAbastecimientoRequest request, String username) {
        Socio suministrador = socioService.recuperarEntidadPorId(request.getSuministradorId());
        if (!suministrador.isEsSuministrador()) {
            throw new BusinessException("El tercero seleccionado no está configurado como proveedor");
        }

        Operario operario = operarioRepository.findByUsername(username)
                .orElseGet(() -> operarioRepository.findByUsername("admin_forja")
                        .orElseGet(() -> operarioRepository.findAll().stream().findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"))));

        String correlativo = secuenciaService.recuperarSiguienteCorrelativo("ABASTECIMIENTO");

        Abastecimiento abastecimiento = new Abastecimiento();
        abastecimiento.setCorrelativo(correlativo);
        abastecimiento.setSuministrador(suministrador);
        abastecimiento.setOperario(operario);
        abastecimiento.setSituacion("RECIBIDA");
        abastecimiento.setMarcaTiempo(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        for (AbastecimientoDetalleRequest det : request.getDetalles()) {
            Ficha fch = catalogoService.recuperarEntidadPorId(det.getFichaId());
            if (!fch.isHabilitado()) {
                throw new BusinessException("No se pueden registrar compras para productos inactivos: " + fch.getCodigoEan());
            }

            BigDecimal subtotal = det.getVolumen().multiply(det.getCosteIndividual()).setScale(2, RoundingMode.HALF_UP);
            total = total.add(subtotal);

            AbastecimientoDetalle detalle = new AbastecimientoDetalle(fch, det.getVolumen(), det.getCosteIndividual(), subtotal);
            abastecimiento.addDetalle(detalle);
        }

        abastecimiento.setTotal(total);
        Abastecimiento abastecimientoGuardada = abastecimientoRepository.save(abastecimiento);

        // Procesar entradas al inventario y actualizar el CPP (RN-04)
        for (AbastecimientoDetalle detalle : abastecimientoGuardada.getDetalles()) {
            almacenService.tramitarEntradaAbastecimiento(
                    detalle.getFicha().getId(),
                    detalle.getVolumen(),
                    detalle.getCosteIndividual(),
                    abastecimientoGuardada.getId(),
                    operario
            );
        }

        bitacoraService.asentarEvento(operario, "ABASTECIMIENTOS", "ASENTAR_ABASTECIMIENTO",
                "Compra registrada " + correlativo + " a proveedor " + suministrador.getNombreLegal() + " por total $" + total);

        return convertirARespuesta(abastecimientoGuardada);
    }

    /**
     * Cancelación de compra por parte del Administrador con motivo documentado (RN-03, RN-08).
     */
    @Transactional
    public AbastecimientoResponse anularAbastecimiento(Long id, AnularFolioRequest request, String username) {
        Abastecimiento abastecimiento = abastecimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con ID: " + id));

        if ("ANULADA".equalsIgnoreCase(abastecimiento.getSituacion())) {
            throw new BusinessException("La compra ya se encuentra cancelada");
        }

        Operario operario = operarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Compensar y revertir stock (bloquea si el stock ya fue consumido)
        for (AbastecimientoDetalle detalle : abastecimiento.getDetalles()) {
            almacenService.compensarSalidaAbastecimiento(
                    detalle.getFicha().getId(),
                    detalle.getVolumen(),
                    detalle.getCosteIndividual(),
                    abastecimiento.getId(),
                    "Anulación de compra " + abastecimiento.getCorrelativo() + ": " + request.getCausa(),
                    operario
            );
        }

        abastecimiento.setSituacion("ANULADA");
        abastecimiento.setCausaAnulacion(request.getCausa());
        Abastecimiento anulada = abastecimientoRepository.save(abastecimiento);

        bitacoraService.asentarEvento(operario, "ABASTECIMIENTOS", "ANULAR_ABASTECIMIENTO",
                "Cancelación de compra " + abastecimiento.getCorrelativo() + ". Motivo: " + request.getCausa());

        return convertirARespuesta(anulada);
    }

    public Abastecimiento recuperarEntidadPorId(Long id) {
        return abastecimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con ID: " + id));
    }

    private AbastecimientoResponse convertirARespuesta(Abastecimiento c) {
        AbastecimientoResponse res = new AbastecimientoResponse();
        res.setId(c.getId());
        res.setCorrelativo(c.getCorrelativo());
        res.setSuministradorId(c.getSuministrador().getId());
        res.setSuministradorNombreLegal(c.getSuministrador().getNombreLegal());
        res.setSuministradorFolio(c.getSuministrador().getNroIdentificacion());
        res.setMarcaTiempo(c.getMarcaTiempo());
        res.setTotal(c.getTotal());
        res.setSituacion(c.getSituacion());
        res.setCausaAnulacion(c.getCausaAnulacion());
        res.setOperarioDenominacion(c.getOperario() != null ? c.getOperario().getNombreVisible() : "SISTEMA");

        var detallesDto = c.getDetalles().stream().map(l -> new AbastecimientoDetalleResponse(
                l.getId(),
                l.getFicha().getId(),
                l.getFicha().getNombreVisible(),
                l.getFicha().getCodigoEan(),
                l.getVolumen(),
                l.getCosteIndividual(),
                l.getSubtotal()
        )).toList();
        res.setDetalles(detallesDto);

        return res;
    }
}
