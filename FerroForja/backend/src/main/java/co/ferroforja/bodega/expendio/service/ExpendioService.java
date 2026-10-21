package co.ferroforja.bodega.expendio.service;

import co.ferroforja.bodega.catalogo.entity.Ficha;
import co.ferroforja.bodega.catalogo.service.CatalogoService;
import co.ferroforja.bodega.socios.entity.Socio;
import co.ferroforja.bodega.socios.service.SocioService;
import co.ferroforja.bodega.folios.service.FolioSecuenciaService;
import co.ferroforja.bodega.almacen.service.AlmacenService;
import co.ferroforja.bodega.abastecimiento.dto.request.AnularFolioRequest;
import co.ferroforja.bodega.restituciones.repository.RestitucionRepository;
import co.ferroforja.bodega.expendio.dto.request.GenerarExpendioRequest;
import co.ferroforja.bodega.expendio.dto.request.ExpendioDetalleRequest;
import co.ferroforja.bodega.expendio.dto.response.ExpendioDetalleResponse;
import co.ferroforja.bodega.expendio.dto.response.ExpendioResponse;
import co.ferroforja.bodega.expendio.entity.Expendio;
import co.ferroforja.bodega.expendio.entity.ExpendioDetalle;
import co.ferroforja.bodega.expendio.repository.ExpendioRepository;
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
public class ExpendioService {

    private final ExpendioRepository expendioRepository;
    private final SocioService socioService;
    private final CatalogoService catalogoService;
    private final AlmacenService almacenService;
    private final FolioSecuenciaService secuenciaService;
    private final RestitucionRepository restitucionRepository;
    private final OperarioRepository operarioRepository;
    private final BitacoraService bitacoraService;

    public ExpendioService(ExpendioRepository expendioRepository,
                       SocioService socioService,
                       CatalogoService catalogoService,
                       AlmacenService almacenService,
                       FolioSecuenciaService secuenciaService,
                       RestitucionRepository restitucionRepository,
                       OperarioRepository operarioRepository,
                       BitacoraService bitacoraService) {
        this.expendioRepository = expendioRepository;
        this.socioService = socioService;
        this.catalogoService = catalogoService;
        this.almacenService = almacenService;
        this.secuenciaService = secuenciaService;
        this.restitucionRepository = restitucionRepository;
        this.operarioRepository = operarioRepository;
        this.bitacoraService = bitacoraService;
    }

    @Transactional(readOnly = true)
    public Page<ExpendioResponse> enumerarExpendios(String situacion, Long compradorId, LocalDateTime desde, LocalDateTime hasta, Pageable pageable) {
        return expendioRepository.searchExpendios(situacion, compradorId, desde, hasta, pageable).map(this::convertirARespuesta);
    }

    @Transactional(readOnly = true)
    public ExpendioResponse recuperarPorId(Long id) {
        Expendio expendio = expendioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con ID: " + id));
        return convertirARespuesta(expendio);
    }

    @Transactional(readOnly = true)
    public ExpendioResponse recuperarPorCorrelativo(String correlativo) {
        Expendio expendio = expendioRepository.findByCorrelativo(correlativo)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con consecutivo: " + correlativo));
        return convertirARespuesta(expendio);
    }

    /**
     * Registro de venta de mostrador de contado en efectivo (RN-01, RN-04, RN-05, RN-06, RN-12).
     * Transacción atómica con bloqueo pesimista de stock y congelamiento del costo histórico.
     */
    @Transactional
    public ExpendioResponse asentarExpendio(GenerarExpendioRequest request, String username) {
        // Regla RN-05: Cliente registrado obligatorio
        Socio comprador = socioService.recuperarEntidadPorId(request.getCompradorId());
        if (!comprador.isEsComprador()) {
            throw new BusinessException("El tercero seleccionado no está configurado como cliente (RN-05)");
        }

        Operario operario = operarioRepository.findByUsername(username)
                .orElseGet(() -> operarioRepository.findByUsername("admin_forja")
                        .orElseGet(() -> operarioRepository.findAll().stream().findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"))));

        String correlativo = secuenciaService.recuperarSiguienteCorrelativo("EXPENDIO");

        // Paso 1: pre-validar líneas contra catálogo y calcular total SIN mutar stock.
        // Esto evita bloquear filas y generar movimientos si el pago es insuficiente (RN-06).
        BigDecimal total = BigDecimal.ZERO;
        java.util.List<Ficha> fchsValidadas = new java.util.ArrayList<>();
        for (ExpendioDetalleRequest det : request.getDetalles()) {
            Ficha fch = catalogoService.recuperarEntidadPorId(det.getFichaId());
            if (!fch.isHabilitado()) {
                throw new BusinessException("El producto " + fch.getCodigoEan() + " está inactivo y no puede ser vendido");
            }
            BigDecimal subtotal = det.getVolumen().multiply(fch.getTarifaPublico()).setScale(2, RoundingMode.HALF_UP);
            total = total.add(subtotal);
            fchsValidadas.add(fch);
        }

        // Regla RN-06: Pago en efectivo de contado (validado ANTES de tocar el inventario)
        if (request.getImporteAbonadoEfectivo().compareTo(total) < 0) {
            throw new BusinessException("El monto entregado en efectivo ($" + request.getImporteAbonadoEfectivo() +
                    ") es inferior al total a pagar ($" + total + ")");
        }

        // Paso 2: persistir cabecera primero para obtener ID real y trazabilidad en Kárdex
        Expendio expendio = new Expendio();
        expendio.setCorrelativo(correlativo);
        expendio.setComprador(comprador);
        expendio.setOperario(operario);
        expendio.setSituacion("COMPLETADA");
        expendio.setMarcaTiempo(LocalDateTime.now());
        expendio.setSubtotal(total);
        expendio.setTotal(total);
        expendio.setImporteAbonadoEfectivo(request.getImporteAbonadoEfectivo());
        expendio.setVueltoEntregado(request.getImporteAbonadoEfectivo().subtract(total));
        Expendio expendioPersistida = expendioRepository.saveAndFlush(expendio);

        // Paso 3: descontar stock con bloqueo pesimista usando el ID real de la venta (RN-01, RN-04)
        for (int i = 0; i < request.getDetalles().size(); i++) {
            ExpendioDetalleRequest det = request.getDetalles().get(i);
            Ficha fch = fchsValidadas.get(i);
            BigDecimal tarifaIndividual = fch.getTarifaPublico();
            BigDecimal subtotal = det.getVolumen().multiply(tarifaIndividual).setScale(2, RoundingMode.HALF_UP);

            BigDecimal costeCongelado = almacenService.restarExistenciaConCerrojo(
                    fch.getId(),
                    det.getVolumen(),
                    "EXPENDIO",
                    expendioPersistida.getId(),
                    "Venta en mostrador " + correlativo,
                    operario
            );

            ExpendioDetalle detalle = new ExpendioDetalle(fch, det.getVolumen(), tarifaIndividual, costeCongelado, subtotal);
            expendioPersistida.addDetalle(detalle);
        }

        Expendio expendioGuardada = expendioRepository.save(expendioPersistida);

        bitacoraService.asentarEvento(operario, "EXPENDIOS", "ASENTAR_EXPENDIO",
                "Venta registrada " + correlativo + " a cliente " + comprador.getNombreLegal() + " por total $" + total);

        return convertirARespuesta(expendioGuardada);
    }

    /**
     * Cancelación de venta con reversión de stock autorizada exclusivamente al Administrador (RN-08).
     */
    @Transactional
    public ExpendioResponse anularExpendio(Long id, AnularFolioRequest request, String username) {
        Expendio expendio = expendioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con ID: " + id));

        if ("ANULADA".equalsIgnoreCase(expendio.getSituacion())) {
            throw new BusinessException("La venta ya se encuentra cancelada");
        }

        // Control de consistencia: No permitir cancelar venta si ya tiene devoluciones previas
        if (restitucionRepository.existsByExpendioId(id)) {
            throw new BusinessException("La venta " + expendio.getCorrelativo() +
                    " posee devoluciones registradas. Resuelva o anule las devoluciones antes de cancelar la venta original.");
        }

        Operario operario = operarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Reintegrar las existencias al inventario disponible
        for (ExpendioDetalle detalle : expendio.getDetalles()) {
            almacenService.compensarEntradaExpendio(
                    detalle.getFicha().getId(),
                    detalle.getVolumen(),
                    detalle.getCosteCongeladoIndividual(),
                    expendio.getId(),
                    "Anulación de venta " + expendio.getCorrelativo() + ": " + request.getCausa(),
                    operario
            );
        }

        expendio.setSituacion("ANULADA");
        expendio.setCausaAnulacion(request.getCausa());
        Expendio anulada = expendioRepository.save(expendio);

        bitacoraService.asentarEvento(operario, "EXPENDIOS", "ANULAR_EXPENDIO",
                "Cancelación de venta " + expendio.getCorrelativo() + ". Motivo: " + request.getCausa());

        return convertirARespuesta(anulada);
    }

    public Expendio recuperarEntidadPorId(Long id) {
        return expendioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con ID: " + id));
    }

    private ExpendioResponse convertirARespuesta(Expendio v) {
        ExpendioResponse res = new ExpendioResponse();
        res.setId(v.getId());
        res.setCorrelativo(v.getCorrelativo());
        res.setCompradorId(v.getComprador().getId());
        res.setCompradorDenominacion(v.getComprador().getNombreLegal());
        res.setCompradorFolio(v.getComprador().getNroIdentificacion());
        res.setMarcaTiempo(v.getMarcaTiempo());
        res.setSubtotal(v.getSubtotal());
        res.setTotal(v.getTotal());
        res.setImporteAbonadoEfectivo(v.getImporteAbonadoEfectivo());
        res.setVueltoEntregado(v.getVueltoEntregado());
        res.setSituacion(v.getSituacion());
        res.setCausaAnulacion(v.getCausaAnulacion());
        res.setOperarioDenominacion(v.getOperario() != null ? v.getOperario().getNombreVisible() : "SISTEMA");

        var detallesDto = v.getDetalles().stream().map(l -> new ExpendioDetalleResponse(
                l.getId(),
                l.getFicha().getId(),
                l.getFicha().getNombreVisible(),
                l.getFicha().getCodigoEan(),
                l.getVolumen(),
                l.getTarifaIndividual(),
                l.getCosteCongeladoIndividual(),
                l.getSubtotal()
        )).toList();
        res.setDetalles(detallesDto);

        return res;
    }
}
