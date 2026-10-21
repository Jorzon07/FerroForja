package co.ferroforja.bodega.restituciones.service;

import co.ferroforja.bodega.catalogo.entity.Ficha;
import co.ferroforja.bodega.catalogo.service.CatalogoService;
import co.ferroforja.bodega.socios.entity.Socio;
import co.ferroforja.bodega.socios.service.SocioService;
import co.ferroforja.bodega.folios.service.FolioSecuenciaService;
import co.ferroforja.bodega.almacen.service.AlmacenService;
import co.ferroforja.bodega.restituciones.dto.request.GenerarRestitucionRequest;
import co.ferroforja.bodega.restituciones.dto.request.RestitucionDetalleRequest;
import co.ferroforja.bodega.restituciones.dto.response.RestitucionDetalleResponse;
import co.ferroforja.bodega.restituciones.dto.response.RestitucionResponse;
import co.ferroforja.bodega.restituciones.entity.Restitucion;
import co.ferroforja.bodega.restituciones.entity.RestitucionDetalle;
import co.ferroforja.bodega.restituciones.repository.RestitucionRepository;
import co.ferroforja.bodega.expendio.entity.Expendio;
import co.ferroforja.bodega.expendio.entity.ExpendioDetalle;
import co.ferroforja.bodega.expendio.service.ExpendioService;
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
import java.time.LocalDateTime;

@Service
public class RestitucionService {

    private final RestitucionRepository restitucionRepository;
    private final ExpendioService expendioService;
    private final SocioService socioService;
    private final CatalogoService catalogoService;
    private final AlmacenService almacenService;
    private final FolioSecuenciaService secuenciaService;
    private final OperarioRepository operarioRepository;
    private final BitacoraService bitacoraService;

    public RestitucionService(RestitucionRepository restitucionRepository,
                         ExpendioService expendioService,
                         SocioService socioService,
                         CatalogoService catalogoService,
                         AlmacenService almacenService,
                         FolioSecuenciaService secuenciaService,
                         OperarioRepository operarioRepository,
                         BitacoraService bitacoraService) {
        this.restitucionRepository = restitucionRepository;
        this.expendioService = expendioService;
        this.socioService = socioService;
        this.catalogoService = catalogoService;
        this.almacenService = almacenService;
        this.secuenciaService = secuenciaService;
        this.operarioRepository = operarioRepository;
        this.bitacoraService = bitacoraService;
    }

    @Transactional(readOnly = true)
    public Page<RestitucionResponse> enumerarRestituciones(Pageable pageable) {
        return restitucionRepository.findAllByOrderByMarcaTiempoDesc(pageable).map(this::convertirARespuesta);
    }

    @Transactional(readOnly = true)
    public RestitucionResponse recuperarPorId(Long id) {
        Restitucion dev = restitucionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Devolución no encontrada con ID: " + id));
        return convertirARespuesta(dev);
    }

    @Transactional(readOnly = true)
    public RestitucionResponse recuperarPorCorrelativo(String correlativo) {
        Restitucion dev = restitucionRepository.findByCorrelativo(correlativo)
                .orElseThrow(() -> new ResourceNotFoundException("Devolución no encontrada con consecutivo: " + correlativo));
        return convertirARespuesta(dev);
    }

    /**
     * Registro de devolución de cliente (RN-04, RN-07).
     * Modalidad vinculada a venta (utiliza costo histórico registrado) o libre (utiliza CPP vigente).
     */
    @Transactional
    public RestitucionResponse asentarRestitucion(GenerarRestitucionRequest request, String username) {
        Operario operario = operarioRepository.findByUsername(username)
                .orElseGet(() -> operarioRepository.findByUsername("admin_forja")
                        .orElseGet(() -> operarioRepository.findAll().stream().findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"))));

        String correlativo = secuenciaService.recuperarSiguienteCorrelativo("RESTITUCION");

        Restitucion restitucion = new Restitucion();
        restitucion.setCorrelativo(correlativo);
        restitucion.setCausa(request.getCausa());
        restitucion.setOperario(operario);
        restitucion.setMarcaTiempo(LocalDateTime.now());
        restitucion.setSituacion("APROBADA");

        boolean esVinculada = request.getExpendioId() != null;
        restitucion.setTipoRestitucion(esVinculada ? "VINCULADA" : "NO_VINCULADA");

        final Expendio expendioOriginal = esVinculada ? expendioService.recuperarEntidadPorId(request.getExpendioId()) : null;
        if (esVinculada) {
            restitucion.setExpendio(expendioOriginal);
            restitucion.setComprador(expendioOriginal.getComprador());
        } else if (request.getCompradorId() != null) {
            Socio comprador = socioService.recuperarEntidadPorId(request.getCompradorId());
            restitucion.setComprador(comprador);
        }

        for (RestitucionDetalleRequest det : request.getDetalles()) {
            Ficha fch = catalogoService.recuperarEntidadPorId(det.getFichaId());
            BigDecimal costeAplicado;

            if (esVinculada) {
                // Buscar la línea correspondiente en la venta original
                final Long fchId = fch.getId();
                ExpendioDetalle detalleExpendio = expendioOriginal.getDetalles().stream()
                        .filter(lv -> lv.getFicha().getId().equals(fchId))
                        .findFirst()
                        .orElseThrow(() -> new BusinessException("El producto " + fch.getCodigoEan() + " no formó parte de la venta original " + expendioOriginal.getCorrelativo()));

                if (det.getVolumen().compareTo(detalleExpendio.getVolumen()) > 0) {
                    throw new BusinessException("La cantidad a devolver (" + det.getVolumen() + ") supera la cantidad vendida (" + detalleExpendio.getVolumen() + ") en la venta " + expendioOriginal.getCorrelativo());
                }

                // Utilizar costo histórico congelado en la venta (RN-04)
                costeAplicado = detalleExpendio.getCosteCongeladoIndividual();
            } else {
                // Utilizar costo promedio ponderado vigente (RN-04)
                costeAplicado = almacenService.recuperarCosteMedioVigente(fch.getId());
            }

            RestitucionDetalle detalle = new RestitucionDetalle(fch, det.getVolumen(), costeAplicado);
            restitucion.addDetalle(detalle);
        }

        // Persistir primero para obtener ID real y usarlo en el Kárdex (trazabilidad RN-07)
        Restitucion devPersistida = restitucionRepository.saveAndFlush(restitucion);

        for (RestitucionDetalle detalle : devPersistida.getDetalles()) {
            // Reincorporar automáticamente al inventario disponible (RN-07)
            almacenService.reponerExistencia(
                    detalle.getFicha().getId(),
                    detalle.getVolumen(),
                    detalle.getCosteIndividualAplicado(),
                    "RESTITUCION",
                    devPersistida.getId(),
                    "Reintegro por devolución " + correlativo + ": " + request.getCausa(),
                    operario
            );
        }

        Restitucion devGuardada = restitucionRepository.save(devPersistida);

        bitacoraService.asentarEvento(operario, "RESTITUCIONES", "ASENTAR_RESTITUCION",
                "Devolución registrada " + correlativo + " (" + restitucion.getTipoRestitucion() + "). Motivo: " + request.getCausa());

        return convertirARespuesta(devGuardada);
    }

    public Restitucion recuperarEntidadPorId(Long id) {
        return restitucionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Devolución no encontrada con ID: " + id));
    }

    private RestitucionResponse convertirARespuesta(Restitucion d) {
        RestitucionResponse res = new RestitucionResponse();
        res.setId(d.getId());
        res.setCorrelativo(d.getCorrelativo());
        if (d.getExpendio() != null) {
            res.setExpendioId(d.getExpendio().getId());
            res.setExpendioCorrelativo(d.getExpendio().getCorrelativo());
        }
        if (d.getComprador() != null) {
            res.setCompradorId(d.getComprador().getId());
            res.setCompradorDenominacion(d.getComprador().getNombreLegal());
        }
        res.setTipoRestitucion(d.getTipoRestitucion());
        res.setMarcaTiempo(d.getMarcaTiempo());
        res.setCausa(d.getCausa());
        res.setSituacion(d.getSituacion());
        res.setOperarioDenominacion(d.getOperario() != null ? d.getOperario().getNombreVisible() : "SISTEMA");

        var detallesDto = d.getDetalles().stream().map(l -> new RestitucionDetalleResponse(
                l.getId(),
                l.getFicha().getId(),
                l.getFicha().getNombreVisible(),
                l.getFicha().getCodigoEan(),
                l.getVolumen(),
                l.getCosteIndividualAplicado()
        )).toList();
        res.setDetalles(detallesDto);

        return res;
    }
}
