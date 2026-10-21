package co.ferroforja.bodega;

import co.ferroforja.bodega.catalogo.entity.Fabricante;
import co.ferroforja.bodega.catalogo.entity.Empaque;
import co.ferroforja.bodega.catalogo.entity.Articulo;
import co.ferroforja.bodega.catalogo.entity.Ficha;
import co.ferroforja.bodega.catalogo.entity.MedidaBase;
import co.ferroforja.bodega.catalogo.repository.FabricanteRepository;
import co.ferroforja.bodega.catalogo.repository.EmpaqueRepository;
import co.ferroforja.bodega.catalogo.repository.ArticuloRepository;
import co.ferroforja.bodega.catalogo.repository.FichaRepository;
import co.ferroforja.bodega.catalogo.repository.MedidaBaseRepository;
import co.ferroforja.bodega.almacen.dto.request.AlmacenAperturaRequest;
import co.ferroforja.bodega.almacen.dto.response.AlmacenNivelResponse;
import co.ferroforja.bodega.almacen.entity.AlmacenNivel;
import co.ferroforja.bodega.almacen.repository.AlmacenNivelRepository;
import co.ferroforja.bodega.almacen.service.AlmacenService;
import co.ferroforja.bodega.comun.exception.ExistenciaInsuficienteException;
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
import java.math.RoundingMode;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AlmacenServiceTest {

    @Autowired
    private AlmacenService almacenService;

    @Autowired
    private AlmacenNivelRepository nivelRepository;

    @Autowired
    private FichaRepository fichaRepository;

    @Autowired
    private ArticuloRepository articuloRepository;

    @Autowired
    private FabricanteRepository fabricanteRepository;

    @Autowired
    private EmpaqueRepository empaqueRepository;

    @Autowired
    private MedidaBaseRepository medidaBaseRepository;

    @Autowired
    private OperarioRepository operarioRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    private Ficha fichaPrueba;
    private Operario operarioPrueba;

    @BeforeEach
    void setUp() {
        Perfil perfilAdmin = perfilRepository.findByDenominacion("ROLE_ADMINISTRADOR")
                .orElseGet(() -> perfilRepository.save(new Perfil(null, "ROLE_ADMINISTRADOR", "Admin")));

        operarioPrueba = operarioRepository.findByUsername("test_user_qa")
                .orElseGet(() -> {
                    Operario u = new Operario();
                    u.setUsername("test_user_qa");
                    u.setPasswordHash("hash");
                    u.setNombreVisible("Usuario QA Test");
                    u.setHabilitado(true);
                    u.setPerfiles(Set.of(perfilAdmin));
                    return operarioRepository.save(u);
                });

        Articulo prod = articuloRepository.findByDenominacion("Tornillo Hexagonal Test")
                .orElseGet(() -> articuloRepository.save(new Articulo(null, "Tornillo Hexagonal Test", "Tornillo de prueba", true)));
        Fabricante fabricante = fabricanteRepository.findByDenominacion("FabricanteQA")
                .orElseGet(() -> fabricanteRepository.save(new Fabricante(null, "FabricanteQA", true)));
        Empaque emp = empaqueRepository.findByDenominacion("Caja x 50 Test")
                .orElseGet(() -> empaqueRepository.save(new Empaque(null, "Caja x 50 Test", true)));
        MedidaBase und = medidaBaseRepository.findByClave("CJA")
                .orElseGet(() -> medidaBaseRepository.save(new MedidaBase(null, "CJA", "Caja")));

        fichaPrueba = fichaRepository.findByCodigoEan("7709999990001")
                .orElseGet(() -> {
                    Ficha r = new Ficha();
                    r.setArticulo(prod);
                    r.setFabricante(fabricante);
                    r.setEmpaque(emp);
                    r.setMedidaBase(und);
                    r.setCodigoEan("7709999990001");
                    r.setTarifaPublico(new BigDecimal("25000.00"));
                    r.setHabilitado(true);
                    return fichaRepository.save(r);
                });
    }

    @Test
    @DisplayName("Inicializar inventario debe registrar saldo y costo promedio ponderado inicial (RN-04)")
    void testInicializarAlmacen() {
        AlmacenAperturaRequest req = new AlmacenAperturaRequest();
        req.setFichaId(fichaPrueba.getId());
        req.setVolumenApertura(new BigDecimal("10.000"));
        req.setCosteIndividual(new BigDecimal("12000.00"));

        AlmacenNivelResponse res = almacenService.inicializarAlmacen(req, operarioPrueba.getUsername());

        assertNotNull(res);
        assertEquals(0, new BigDecimal("10.000").compareTo(res.getVolumenLibre()));
        assertEquals(0, new BigDecimal("12000.0000").compareTo(res.getCosteMedioPonderado()));
    }

    @Test
    @DisplayName("Cálculo matemático exacto del Costo Promedio Ponderado tras compra sucesiva (RN-04)")
    void testRecalculoCosteMedioPonderado() {
        // 1. Saldo actual: 10 unidades a $2.000 = $20.000 valor inventario
        AlmacenNivel nivel = nivelRepository.findByFichaId(fichaPrueba.getId())
                .orElseGet(() -> new AlmacenNivel(fichaPrueba, BigDecimal.ZERO, BigDecimal.ZERO));
        nivel.setVolumenLibre(new BigDecimal("10.000"));
        nivel.setCosteMedioPonderado(new BigDecimal("2000.0000"));
        nivelRepository.save(nivel);

        // 2. Compra nueva: 10 unidades a $3.000 = $30.000 valor compra
        // Nuevo CPP esperado: (20.000 + 30.000) / 20 = 50.000 / 20 = $2.500
        almacenService.tramitarEntradaAbastecimiento(
                fichaPrueba.getId(),
                new BigDecimal("10.000"),
                new BigDecimal("3000.0000"),
                1L,
                operarioPrueba
        );

        AlmacenNivel nivelActualizado = nivelRepository.findByFichaId(fichaPrueba.getId()).orElseThrow();

        assertEquals(0, new BigDecimal("20.000").compareTo(nivelActualizado.getVolumenLibre()));
        assertEquals(0, new BigDecimal("2500.0000").compareTo(nivelActualizado.getCosteMedioPonderado().setScale(4, RoundingMode.HALF_UP)));
    }

    @Test
    @DisplayName("Descontar existencias debe fallar con ExistenciaInsuficienteException si supera el stock disponible (RN-01)")
    void testAntiExistenciaNegativa() {
        AlmacenNivel nivel = nivelRepository.findByFichaId(fichaPrueba.getId())
                .orElseGet(() -> new AlmacenNivel(fichaPrueba, BigDecimal.ZERO, BigDecimal.ZERO));
        nivel.setVolumenLibre(new BigDecimal("3.000"));
        nivel.setCosteMedioPonderado(new BigDecimal("5000.0000"));
        nivelRepository.save(nivel);

        // Intentar descontar 5 unidades cuando solo hay 3 disponibles
        assertThrows(ExistenciaInsuficienteException.class, () -> {
            almacenService.restarExistenciaConCerrojo(
                    fichaPrueba.getId(),
                    new BigDecimal("5.000"),
                    "EXPENDIO",
                    1L,
                    "Venta de prueba",
                    operarioPrueba
            );
        });

        // Verificar que el stock permanezca en 3 unidades y nunca negativo
        AlmacenNivel nivelFinal = nivelRepository.findByFichaId(fichaPrueba.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("3.000").compareTo(nivelFinal.getVolumenLibre()));
    }
}
