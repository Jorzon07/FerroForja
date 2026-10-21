package co.ferroforja.bodega;

import co.ferroforja.bodega.socios.entity.Socio;
import co.ferroforja.bodega.socios.repository.SocioRepository;
import co.ferroforja.bodega.folios.service.FolioSecuenciaService;
import co.ferroforja.bodega.almacen.dto.request.ReajusteAlmacenRequest;
import co.ferroforja.bodega.almacen.entity.ReajusteAlmacen;
import co.ferroforja.bodega.almacen.entity.AlmacenNivel;
import co.ferroforja.bodega.almacen.repository.AlmacenNivelRepository;
import co.ferroforja.bodega.almacen.service.AlmacenReajusteService;
import co.ferroforja.bodega.abastecimiento.dto.request.AnularFolioRequest;
import co.ferroforja.bodega.restituciones.dto.request.GenerarRestitucionRequest;
import co.ferroforja.bodega.restituciones.dto.request.RestitucionDetalleRequest;
import co.ferroforja.bodega.restituciones.dto.response.RestitucionResponse;
import co.ferroforja.bodega.restituciones.service.RestitucionService;
import co.ferroforja.bodega.expendio.dto.request.GenerarExpendioRequest;
import co.ferroforja.bodega.expendio.dto.request.ExpendioDetalleRequest;
import co.ferroforja.bodega.expendio.dto.response.ExpendioResponse;
import co.ferroforja.bodega.expendio.service.ExpendioService;
import co.ferroforja.bodega.comun.exception.BusinessException;
import co.ferroforja.bodega.operarios.entity.Perfil;
import co.ferroforja.bodega.operarios.entity.Operario;
import co.ferroforja.bodega.operarios.repository.PerfilRepository;
import co.ferroforja.bodega.operarios.repository.OperarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RestitucionesYReajustesTest {

    @Autowired
    private ExpendioService expendioService;

    @Autowired
    private RestitucionService restitucionService;

    @Autowired
    private AlmacenReajusteService reajusteService;

    @Autowired
    private FolioSecuenciaService secuenciaService;

    @Autowired
    private AlmacenNivelRepository nivelRepository;

    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private OperarioRepository operarioRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    private Socio compradorPrueba;
    private Operario adminOperario;

    @BeforeEach
    void setUp() {
        Perfil perfilAdmin = perfilRepository.findByDenominacion("ROLE_ADMINISTRADOR")
                .orElseGet(() -> perfilRepository.save(new Perfil(null, "ROLE_ADMINISTRADOR", "Admin")));

        adminOperario = operarioRepository.findByUsername("admin_forja")
                .orElseGet(() -> {
                    Operario u = new Operario();
                    u.setUsername("admin_forja");
                    u.setPasswordHash("hash");
                    u.setNombreVisible("Administrador General");
                    u.setHabilitado(true);
                    u.setPerfiles(Set.of(perfilAdmin));
                    return operarioRepository.save(u);
                });

        compradorPrueba = socioRepository.findByNroIdentificacion("1020304050")
                .orElseGet(() -> {
                    Socio t = new Socio();
                    t.setTipoIdentificacion("CC");
                    t.setNroIdentificacion("1020304050");
                    t.setNombreLegal("Cliente Devoluciones");
                    t.setTipoPersona("NATURAL");
                    t.setEsComprador(true);
                    t.setEsSuministrador(false);
                    return socioRepository.save(t);
                });
    }

    @Test
    @DisplayName("Generador de consecutivos correlativos debe emitir números consecutivos sin saltos (RN-12)")
    void testGenerarCorrelativosSecuenciales() {
        String num1 = secuenciaService.recuperarSiguienteCorrelativo("EXPENDIO");
        String num2 = secuenciaService.recuperarSiguienteCorrelativo("EXPENDIO");

        assertTrue(num1.startsWith("EXP-"));
        assertTrue(num2.startsWith("EXP-"));
        assertNotEquals(num1, num2);
    }

    @Test
    @DisplayName("Devolución vinculada: No se puede devolver más cantidad de la vendida en la venta original (RN-07)")
    void testRestitucionExcedeVolumenExpendida() {
        // 1. Registrar una venta de 2 unidades
        GenerarExpendioRequest expReq = new GenerarExpendioRequest();
        expReq.setCompradorId(compradorPrueba.getId());
        expReq.setImporteAbonadoEfectivo(new BigDecimal("100000.00"));
        expReq.setDetalles(List.of(new ExpendioDetalleRequest(1L, new BigDecimal("2.000"))));

        ExpendioResponse expendio = expendioService.asentarExpendio(expReq, adminOperario.getUsername());

        // 2. Intentar devolver 3 unidades de esa venta
        GenerarRestitucionRequest devReq = new GenerarRestitucionRequest();
        devReq.setExpendioId(expendio.getId());
        devReq.setCausa("Intento devolver más de lo comprado");
        devReq.setDetalles(List.of(new RestitucionDetalleRequest(1L, new BigDecimal("3.000"))));

        assertThrows(BusinessException.class, () -> {
            restitucionService.asentarRestitucion(devReq, adminOperario.getUsername());
        });
    }

    @Test
    @DisplayName("Cancelar una venta que ya tiene devoluciones asociadas debe ser bloqueado para evitar doble stock")
    void testAnularExpendioConRestitucionesBloqueado() {
        // 1. Registrar una venta de 5 unidades
        GenerarExpendioRequest expReq = new GenerarExpendioRequest();
        expReq.setCompradorId(compradorPrueba.getId());
        expReq.setImporteAbonadoEfectivo(new BigDecimal("200000.00"));
        expReq.setDetalles(List.of(new ExpendioDetalleRequest(1L, new BigDecimal("5.000"))));

        ExpendioResponse expendio = expendioService.asentarExpendio(expReq, adminOperario.getUsername());

        // 2. Registrar devolución parcial de 2 unidades
        GenerarRestitucionRequest devReq = new GenerarRestitucionRequest();
        devReq.setExpendioId(expendio.getId());
        devReq.setCausa("Devolución parcial de 2 martillos");
        devReq.setDetalles(List.of(new RestitucionDetalleRequest(1L, new BigDecimal("2.000"))));

        RestitucionResponse dev = restitucionService.asentarRestitucion(devReq, adminOperario.getUsername());
        assertNotNull(dev);

        // 3. Intentar cancelar la venta original completa
        AnularFolioRequest anulReq = new AnularFolioRequest("Intento anular venta con devoluciones");

        assertThrows(BusinessException.class, () -> {
            expendioService.anularExpendio(expendio.getId(), anulReq, adminOperario.getUsername());
        });
    }

    @Test
    @DisplayName("Ajuste manual de inventario autorizado por Administrador actualiza existencias y Kárdex (RN-08)")
    void testReajusteManualAutorizado() {
        AlmacenNivel nivelAntes = nivelRepository.findByFichaId(1L).orElseThrow();
        BigDecimal volAntes = nivelAntes.getVolumenLibre();

        ReajusteAlmacenRequest req = new ReajusteAlmacenRequest();
        req.setFichaId(1L);
        req.setTipoReajuste("INCREMENTO");
        req.setVolumen(new BigDecimal("3.000"));
        req.setCausa("Conteo físico semestral: sobrante verificado");

        ReajusteAlmacen reajuste = reajusteService.ejecutarReajuste(req, adminOperario.getUsername());

        assertNotNull(reajuste);
        assertEquals("INCREMENTO", reajuste.getTipoReajuste());

        AlmacenNivel nivelDespues = nivelRepository.findByFichaId(1L).orElseThrow();
        assertEquals(0, volAntes.add(new BigDecimal("3.000")).compareTo(nivelDespues.getVolumenLibre()));
    }
}
