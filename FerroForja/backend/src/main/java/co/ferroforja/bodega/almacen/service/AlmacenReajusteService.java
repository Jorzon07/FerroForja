package co.ferroforja.bodega.almacen.service;

import co.ferroforja.bodega.catalogo.entity.Ficha;
import co.ferroforja.bodega.catalogo.service.CatalogoService;
import co.ferroforja.bodega.folios.service.FolioSecuenciaService;
import co.ferroforja.bodega.almacen.dto.request.ReajusteAlmacenRequest;
import co.ferroforja.bodega.almacen.entity.ReajusteAlmacen;
import co.ferroforja.bodega.almacen.repository.ReajusteAlmacenRepository;
import co.ferroforja.bodega.comun.bitacora.BitacoraService;
import co.ferroforja.bodega.comun.exception.ResourceNotFoundException;
import co.ferroforja.bodega.operarios.entity.Operario;
import co.ferroforja.bodega.operarios.repository.OperarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Servicio de Ajustes Manuales de Inventario:
 * Ejecutable y autorizable exclusivamente por usuarios con rol ADMINISTRADOR (RN-08).
 */
@Service
public class AlmacenReajusteService {

    private final ReajusteAlmacenRepository reajusteRepository;
    private final AlmacenService almacenService;
    private final CatalogoService catalogoService;
    private final FolioSecuenciaService secuenciaService;
    private final OperarioRepository operarioRepository;
    private final BitacoraService bitacoraService;

    public AlmacenReajusteService(ReajusteAlmacenRepository reajusteRepository,
                                      AlmacenService almacenService,
                                      CatalogoService catalogoService,
                                      FolioSecuenciaService secuenciaService,
                                      OperarioRepository operarioRepository,
                                      BitacoraService bitacoraService) {
        this.reajusteRepository = reajusteRepository;
        this.almacenService = almacenService;
        this.catalogoService = catalogoService;
        this.secuenciaService = secuenciaService;
        this.operarioRepository = operarioRepository;
        this.bitacoraService = bitacoraService;
    }

    @Transactional(readOnly = true)
    public Page<ReajusteAlmacen> enumerarReajustes(Pageable pageable) {
        return reajusteRepository.findAllByOrderByMarcaTiempoDesc(pageable);
    }

    @Transactional
    public ReajusteAlmacen ejecutarReajuste(ReajusteAlmacenRequest request, String adminUsername) {
        Operario admin = operarioRepository.findByUsername(adminUsername)
                .orElseGet(() -> operarioRepository.findByUsername("admin_forja")
                        .orElseGet(() -> operarioRepository.findAll().stream().findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException("Administrador no encontrado"))));

        Ficha fch = catalogoService.recuperarEntidadPorId(request.getFichaId());
        String correlativo = secuenciaService.recuperarSiguienteCorrelativo("REAJUSTE");
        BigDecimal cppVigente = almacenService.recuperarCosteMedioVigente(fch.getId());

        ReajusteAlmacen reajuste = new ReajusteAlmacen();
        reajuste.setCorrelativo(correlativo);
        reajuste.setFicha(fch);
        reajuste.setTipoReajuste(request.getTipoReajuste());
        reajuste.setVolumen(request.getVolumen());
        reajuste.setCosteIndividualFicha(cppVigente);
        reajuste.setCausa(request.getCausa());
        reajuste.setMarcaTiempo(LocalDateTime.now());
        reajuste.setOperario(admin);

        ReajusteAlmacen guardado = reajusteRepository.save(reajuste);

        // Actualizar saldo y Kárdex (RN-08)
        almacenService.tramitarReajusteManual(
                fch.getId(),
                request.getTipoReajuste(),
                request.getVolumen(),
                request.getCausa(),
                guardado.getId(),
                admin
        );

        bitacoraService.asentarEvento(admin, "REAJUSTES", "REAJUSTE_ALMACEN",
                "Ajuste manual " + correlativo + " (" + request.getTipoReajuste() + " de " + request.getVolumen() +
                        " unidades en " + fch.getCodigoEan() + "). Motivo: " + request.getCausa());

        return guardado;
    }
}
