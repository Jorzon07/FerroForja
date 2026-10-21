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

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de concurrencia e integridad transaccional (RN-01):
 * Simula múltiples hilos ejecutando salidas simultáneas sobre un stock limitado
 * para comprobar que el bloqueo pesimista impida que el inventario quede negativo.
 */
@SpringBootTest
@ActiveProfiles("test")
class ConcurrenciaAlmacenTest {

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

    private Ficha fichaConcurrente;
    private Operario operarioPrueba;

    @BeforeEach
    void setUp() {
        Perfil perfilAdmin = perfilRepository.findByDenominacion("ROLE_ADMINISTRADOR")
                .orElseGet(() -> perfilRepository.save(new Perfil(null, "ROLE_ADMINISTRADOR", "Admin")));

        operarioPrueba = operarioRepository.findByUsername("user_concurrencia")
                .orElseGet(() -> {
                    Operario u = new Operario();
                    u.setUsername("user_concurrencia");
                    u.setPasswordHash("hash");
                    u.setNombreVisible("Usuario Concurrencia");
                    u.setHabilitado(true);
                    u.setPerfiles(Set.of(perfilAdmin));
                    return operarioRepository.save(u);
                });

        Articulo prod = articuloRepository.findByDenominacion("Cinta Concurrente")
                .orElseGet(() -> articuloRepository.save(new Articulo(null, "Cinta Concurrente", "Producto prueba", true)));
        Fabricante fabricante = fabricanteRepository.findByDenominacion("FabricanteConc")
                .orElseGet(() -> fabricanteRepository.save(new Fabricante(null, "FabricanteConc", true)));
        Empaque emp = empaqueRepository.findByDenominacion("RolloConc")
                .orElseGet(() -> empaqueRepository.save(new Empaque(null, "RolloConc", true)));
        MedidaBase und = medidaBaseRepository.findByClave("UND")
                .orElseGet(() -> medidaBaseRepository.save(new MedidaBase(null, "UND", "Unidad")));

        fichaConcurrente = fichaRepository.findByCodigoEan("7709999990999")
                .orElseGet(() -> {
                    Ficha r = new Ficha();
                    r.setArticulo(prod);
                    r.setFabricante(fabricante);
                    r.setEmpaque(emp);
                    r.setMedidaBase(und);
                    r.setCodigoEan("7709999990999");
                    r.setTarifaPublico(new BigDecimal("10000.00"));
                    r.setHabilitado(true);
                    return fichaRepository.save(r);
                });

        // Establecer un saldo exactamente de 5 unidades para la prueba
        AlmacenNivel nivel = nivelRepository.findByFichaId(fichaConcurrente.getId())
                .orElseGet(() -> new AlmacenNivel(fichaConcurrente, BigDecimal.ZERO, BigDecimal.ZERO));
        nivel.setVolumenLibre(new BigDecimal("5.000"));
        nivel.setCosteMedioPonderado(new BigDecimal("6000.0000"));
        nivelRepository.save(nivel);
    }

    @Test
    @DisplayName("Concurrencia: 10 hilos intentando descontar 1 unidad sobre un stock de 5 deben resultar en 5 éxitos, 5 rechazos y stock final en 0 (RN-01)")
    void testConcurrenciaExpendiosExistenciaNegativa() throws InterruptedException {
        int totalHilos = 10;
        ExecutorService executor = Executors.newFixedThreadPool(totalHilos);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalHilos);

        AtomicInteger exitos = new AtomicInteger(0);
        AtomicInteger fallosExistencia = new AtomicInteger(0);

        for (int i = 0; i < totalHilos; i++) {
            executor.submit(() -> {
                try {
                    latch.await(); // Esperar a que todos los hilos estén listos para disparar simultáneamente
                    almacenService.restarExistenciaConCerrojo(
                            fichaConcurrente.getId(),
                            new BigDecimal("1.000"),
                            "EXPENDIO",
                            999L,
                            "Venta concurrente de prueba",
                            operarioPrueba
                    );
                    exitos.incrementAndGet();
                } catch (ExistenciaInsuficienteException e) {
                    fallosExistencia.incrementAndGet();
                } catch (Exception e) {
                    // Otras excepciones transaccionales
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown(); // Disparar todos los hilos concurrentes
        doneLatch.await(); // Esperar a que todos terminen
        executor.shutdown();

        // Verificar resultados de la contienda concurrente
        assertEquals(5, exitos.get(), "Exactamente 5 transacciones debieron completarse exitosamente");
        assertEquals(5, fallosExistencia.get(), "Exactamente 5 transacciones debieron ser rechazadas por falta de existencias");

        AlmacenNivel nivelFinal = nivelRepository.findByFichaId(fichaConcurrente.getId()).orElseThrow();
        assertEquals(0, BigDecimal.ZERO.compareTo(nivelFinal.getVolumenLibre()),
                "El saldo disponible final debe ser exactamente 0.000 y nunca un valor negativo");
    }
}
