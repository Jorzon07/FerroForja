package co.ferroforja.bodega.configuracion;

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
import co.ferroforja.bodega.socios.entity.Socio;
import co.ferroforja.bodega.socios.repository.SocioRepository;
import co.ferroforja.bodega.folios.entity.TiqueteCorrelativo;
import co.ferroforja.bodega.folios.repository.TiqueteCorrelativoRepository;
import co.ferroforja.bodega.almacen.entity.AlmacenAsiento;
import co.ferroforja.bodega.almacen.entity.AlmacenNivel;
import co.ferroforja.bodega.almacen.repository.AlmacenAsientoRepository;
import co.ferroforja.bodega.almacen.repository.AlmacenNivelRepository;
import co.ferroforja.bodega.operarios.entity.Perfil;
import co.ferroforja.bodega.operarios.entity.Operario;
import co.ferroforja.bodega.operarios.repository.PerfilRepository;
import co.ferroforja.bodega.operarios.repository.OperarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Componente de aprovisionamiento inicial seguro:
 * - Inicializa roles y usuarios operativos y administradores.
 * - Asegura la inicialización de consecutivos y catálogo representativo de ferretería colombiana.
 */
@Component
public class AdminInitializerRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializerRunner.class);

    private final OperarioRepository operarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;
    private final TiqueteCorrelativoRepository correlativoRepository;
    private final MedidaBaseRepository medidaBaseRepository;
    private final ArticuloRepository articuloRepository;
    private final FabricanteRepository fabricanteRepository;
    private final EmpaqueRepository empaqueRepository;
    private final FichaRepository fichaRepository;
    private final AlmacenNivelRepository nivelRepository;
    private final AlmacenAsientoRepository asientoRepository;
    private final SocioRepository socioRepository;

    @Value("${app.admin.initial.username:gerente_forja}")
    private String initialAdminUsername;

    @Value("${app.admin.initial.password:Forja#2026Master!}")
    private String initialAdminPassword;

    public AdminInitializerRunner(OperarioRepository operarioRepository,
                                  PerfilRepository perfilRepository,
                                  PasswordEncoder passwordEncoder,
                                  TiqueteCorrelativoRepository correlativoRepository,
                                  MedidaBaseRepository medidaBaseRepository,
                                  ArticuloRepository articuloRepository,
                                  FabricanteRepository fabricanteRepository,
                                  EmpaqueRepository empaqueRepository,
                                  FichaRepository fichaRepository,
                                  AlmacenNivelRepository nivelRepository,
                                  AlmacenAsientoRepository asientoRepository,
                                  SocioRepository socioRepository) {
        this.operarioRepository = operarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
        this.correlativoRepository = correlativoRepository;
        this.medidaBaseRepository = medidaBaseRepository;
        this.articuloRepository = articuloRepository;
        this.fabricanteRepository = fabricanteRepository;
        this.empaqueRepository = empaqueRepository;
        this.fichaRepository = fichaRepository;
        this.nivelRepository = nivelRepository;
        this.asientoRepository = asientoRepository;
        this.socioRepository = socioRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Roles obligatorios
        Perfil perfilAdmin = perfilRepository.findByDenominacion("ROLE_ADMINISTRADOR")
                .orElseGet(() -> perfilRepository.save(new Perfil(null, "ROLE_ADMINISTRADOR", "Acceso total administrativo")));
        Perfil perfilOperativo = perfilRepository.findByDenominacion("ROLE_USUARIO_OPERATIVO")
                .orElseGet(() -> perfilRepository.save(new Perfil(null, "ROLE_USUARIO_OPERATIVO", "Usuario operativo de mostrador")));

        // 2. Usuarios del sistema
        // A. Gerente General (Administrador Principal)
        crearOActualizarOperario("gerente_forja", "Forja#2026Master!", "Ing. Germán Ortiz - Gerente General", "gerencia@ferroforja.com.co", perfilAdmin);

        // B. Jefe de Almacén y Control de Stock (Administrador)
        crearOActualizarOperario("jefe_almacen", "Bodega#2026Segura!", "Andrés Vega - Jefe de Almacén y Control de Stock", "almacen@ferroforja.com.co", perfilAdmin);

        // C. Cajera Principal Mostrador (Usuario Operativo)
        crearOActualizarOperario("cajero_mostrador", "Caja#2026Mostrador!", "Daniela Morales - Cajera Principal Mostrador", "caja@ferroforja.com.co", perfilOperativo);

        // D. Soporte y Fallback Técnico (admin_forja)
        Operario adminPrincipal = crearOActualizarOperario("admin_forja", "Forja#Admin26", "Administrador Técnico de Sistema", "admin@ferroforja.local", perfilAdmin);

        // 3. Consecutivos iniciales (RN-12)
        correlativoRepository.findByTipoIdentificacion("EXPENDIO")
                .orElseGet(() -> correlativoRepository.save(new TiqueteCorrelativo("EXPENDIO", "EXP-", 100L)));
        correlativoRepository.findByTipoIdentificacion("ABASTECIMIENTO")
                .orElseGet(() -> correlativoRepository.save(new TiqueteCorrelativo("ABASTECIMIENTO", "CMP-", 50L)));
        correlativoRepository.findByTipoIdentificacion("RESTITUCION")
                .orElseGet(() -> correlativoRepository.save(new TiqueteCorrelativo("RESTITUCION", "DEV-", 10L)));
        correlativoRepository.findByTipoIdentificacion("REAJUSTE")
                .orElseGet(() -> correlativoRepository.save(new TiqueteCorrelativo("REAJUSTE", "AJU-", 5L)));

        // 4. Catálogos base si están vacíos
        if (medidaBaseRepository.count() == 0) {
            // Unidades de medida
            MedidaBase und = medidaBaseRepository.save(new MedidaBase(null, "UND", "Unidad"));
            MedidaBase mtr = medidaBaseRepository.save(new MedidaBase(null, "MTR", "Metro lineal"));
            MedidaBase kgm = medidaBaseRepository.save(new MedidaBase(null, "KGM", "Kilogramo"));
            MedidaBase gln = medidaBaseRepository.save(new MedidaBase(null, "GLN", "Galón"));
            MedidaBase cja = medidaBaseRepository.save(new MedidaBase(null, "CJA", "Caja"));
            MedidaBase blt = medidaBaseRepository.save(new MedidaBase(null, "BLT", "Bulto"));

            // Fabricantes
            Fabricante mHerragro = fabricanteRepository.save(new Fabricante(null, "Herragro", true));
            Fabricante mStanley = fabricanteRepository.save(new Fabricante(null, "Stanley", true));
            Fabricante mPintuco = fabricanteRepository.save(new Fabricante(null, "Pintuco", true));
            Fabricante mPavco = fabricanteRepository.save(new Fabricante(null, "Pavco Wavin", true));
            Fabricante mBellota = fabricanteRepository.save(new Fabricante(null, "Bellota", true));
            Fabricante mSika = fabricanteRepository.save(new Fabricante(null, "Sika Colombia", true));
            Fabricante mBosch = fabricanteRepository.save(new Fabricante(null, "Bosch", true));
            Fabricante mGyplac = fabricanteRepository.save(new Fabricante(null, "Gyplac", true));
            Fabricante mGerfor = fabricanteRepository.save(new Fabricante(null, "Gerfor", true));
            Fabricante mAcesco = fabricanteRepository.save(new Fabricante(null, "Acesco", true));

            // Empaques
            Empaque pUnidad = empaqueRepository.save(new Empaque(null, "Unidad Suelta", true));
            Empaque pCaja100 = empaqueRepository.save(new Empaque(null, "Caja x 100", true));
            Empaque pCaja500 = empaqueRepository.save(new Empaque(null, "Caja x 500", true));
            Empaque pGalon = empaqueRepository.save(new Empaque(null, "Galón 3.78L", true));
            Empaque pCunete = empaqueRepository.save(new Empaque(null, "Cuñete 5 Galones", true));
            Empaque pTubo6m = empaqueRepository.save(new Empaque(null, "Tubo 6 Metros", true));
            Empaque pRollo50 = empaqueRepository.save(new Empaque(null, "Rollo 50m", true));
            Empaque pBulto = empaqueRepository.save(new Empaque(null, "Bulto 50 Kg", true));
            Empaque pBolsa1k = empaqueRepository.save(new Empaque(null, "Bolsa 1 Kg", true));
            Empaque pBlister = empaqueRepository.save(new Empaque(null, "Kit / Blíster", true));

            // Artículos
            Articulo a1 = articuloRepository.save(new Articulo(null, "Martillo de Uña 16oz Forjado", "Martillo forjado de acero con mango de fibra de vidrio ergonómico", true));
            Articulo a2 = articuloRepository.save(new Articulo(null, "Tornillo Drywall 1 1/4 pulg Fosfatado", "Tornillo de fijación para láminas de yeso y fibrocemento punta broca", true));
            Articulo a3 = articuloRepository.save(new Articulo(null, "Pintura Vinilo Tipo 1 Blanco Pro", "Pintura base agua de alta lavabilidad para interiores y fachadas", true));
            Articulo a4 = articuloRepository.save(new Articulo(null, "Tubo PVC Presión RDE 9 1/2 pulg x 6m", "Tubo para conducción de agua potable a presión norma NTC 382", true));
            Articulo a5 = articuloRepository.save(new Articulo(null, "Alicate Universal Electricista 8 pulg Aislado", "Alicate forjado con aislamiento 1000V para electricista", true));
            Articulo a6 = articuloRepository.save(new Articulo(null, "Taladro Percutor 1/2 pulg 650W GSB 13 RE", "Taladro percutor industrial con velocidad variable y reversible", true));
            Articulo a7 = articuloRepository.save(new Articulo(null, "Impermeabilizante Sika 1 Líquido Frasco 4 Kg", "Aditivo hidrófugo de fraguado normal para morteros y pañetes", true));
            Articulo a8 = articuloRepository.save(new Articulo(null, "Pala Redonda Herrada No. 4 Cabo Madera", "Pala para mezcla, excavación y carga en obra civil", true));
            Articulo a9 = articuloRepository.save(new Articulo(null, "Disco de Corte Metal 4 1/2 pulg x 1 mm Standard", "Disco abrasivo fino para corte rápido de acero y perfiles", true));
            Articulo a10 = articuloRepository.save(new Articulo(null, "Cinta Métrica Global Plus 5m / 16ft", "Flexómetro de alta visibilidad con cinta recubierta en polímero", true));
            Articulo a11 = articuloRepository.save(new Articulo(null, "Cemento Gris Uso General Tipo UG Bulto 50kg", "Cemento estructural para fundición de columnas, placas y vigas", true));
            Articulo a12 = articuloRepository.save(new Articulo(null, "Llave de Tubo Stilson 12 pulg Heavy Duty", "Llave para tubería y fontanería pesada con mandíbula forjada", true));
            Articulo a13 = articuloRepository.save(new Articulo(null, "Anticorrosivo Alquídico Rojo Óxido 1 Galón", "Pintura base anticorrosiva para metales ferrosos expuestos", true));
            Articulo a14 = articuloRepository.save(new Articulo(null, "Soldadura Líquida PVC Presión 1/4 Galón", "Pegante de viscosidad media para unión hermética de tubos PVC", true));
            Articulo a15 = articuloRepository.save(new Articulo(null, "Guante de Vaqueta Tipo Ingeniero Reforzado", "Guante de cuero vacuno para protección manual en trabajo pesado", true));

            // Fichas (Referencias comerciales con EAN, Precio venta, CPP y Stock inicial)
            registrarFichaConStock(a1, mHerragro, pUnidad, und, "7701234567891", new BigDecimal("38500.00"), new BigDecimal("25.000"), new BigDecimal("26000.0000"), adminPrincipal);
            registrarFichaConStock(a2, mGyplac, pCaja100, cja, "7701234567892", new BigDecimal("16900.00"), new BigDecimal("60.000"), new BigDecimal("10500.0000"), adminPrincipal);
            registrarFichaConStock(a3, mPintuco, pGalon, gln, "7701234567893", new BigDecimal("84900.00"), new BigDecimal("18.000"), new BigDecimal("61000.0000"), adminPrincipal);
            registrarFichaConStock(a4, mPavco, pTubo6m, mtr, "7701234567894", new BigDecimal("21500.00"), new BigDecimal("40.000"), new BigDecimal("14800.0000"), adminPrincipal);
            registrarFichaConStock(a5, mStanley, pUnidad, und, "7701234567895", new BigDecimal("46000.00"), new BigDecimal("15.000"), new BigDecimal("31500.0000"), adminPrincipal);
            registrarFichaConStock(a6, mBosch, pUnidad, und, "7701234567896", new BigDecimal("269900.00"), new BigDecimal("8.000"), new BigDecimal("198000.0000"), adminPrincipal);
            registrarFichaConStock(a7, mSika, pGalon, gln, "7701234567897", new BigDecimal("42500.00"), new BigDecimal("22.000"), new BigDecimal("29000.0000"), adminPrincipal);
            registrarFichaConStock(a8, mBellota, pUnidad, und, "7701234567898", new BigDecimal("54000.00"), new BigDecimal("16.000"), new BigDecimal("37500.0000"), adminPrincipal);
            registrarFichaConStock(a9, mBosch, pCaja100, cja, "7701234567899", new BigDecimal("6200.00"), new BigDecimal("120.000"), new BigDecimal("3900.0000"), adminPrincipal);
            registrarFichaConStock(a10, mStanley, pUnidad, und, "7701234567800", new BigDecimal("28900.00"), new BigDecimal("30.000"), new BigDecimal("19200.0000"), adminPrincipal);
            registrarFichaConStock(a11, mAcesco, pBulto, blt, "7701234567801", new BigDecimal("34000.00"), new BigDecimal("75.000"), new BigDecimal("26800.0000"), adminPrincipal);
            registrarFichaConStock(a12, mStanley, pUnidad, und, "7701234567802", new BigDecimal("62000.00"), new BigDecimal("12.000"), new BigDecimal("43500.0000"), adminPrincipal);
            registrarFichaConStock(a13, mPintuco, pGalon, gln, "7701234567803", new BigDecimal("76500.00"), new BigDecimal("14.000"), new BigDecimal("53000.0000"), adminPrincipal);
            registrarFichaConStock(a14, mPavco, pUnidad, und, "7701234567804", new BigDecimal("36800.00"), new BigDecimal("25.000"), new BigDecimal("25200.0000"), adminPrincipal);
            registrarFichaConStock(a15, mBellota, pUnidad, und, "7701234567805", new BigDecimal("19500.00"), new BigDecimal("45.000"), new BigDecimal("12800.0000"), adminPrincipal);

            // Terceros (Socios): Proveedores
            crearSocio("NIT", "900543210-1", "Distribuidora Ferretera Nacional S.A.S.", "EMPRESA", "6013204500", "ventas@ferrenacional.local", "Zona Industrial Bogotá", false, true);
            crearSocio("NIT", "890101456-2", "Compañía Colombiana de Pinturas Pintuco S.A.", "EMPRESA", "6044445566", "pedidos@pintuco.local", "Medellín, Antioquia", false, true);
            crearSocio("NIT", "860012345-9", "Pavco Wavin Colombia S.A.S.", "EMPRESA", "6017823344", "atencion@pavcowavin.local", "Autopista Sur Km 11, Soacha", false, true);
            crearSocio("NIT", "901056789-3", "Bosch Herramientas y Maquinaria Colombia S.A.S.", "EMPRESA", "6026543210", "distribuidores@bosch.local", "Zona Franca del Pacífico, Cali", false, true);

            // Terceros (Socios): Clientes
            crearSocio("CC", "1020304050", "Juan Carlos Pérez Rodríguez", "NATURAL", "3109876543", "juan.perez@constructor.local", "Carrera 15 # 45-20, Bogotá", true, false);
            crearSocio("CC", "1098765432", "María Fernanda López Gómez", "NATURAL", "3201234567", "maria.lopez@arquitectura.local", "Calle 80 # 24-10, Bogotá", true, false);
            crearSocio("CC", "1018456123", "Andrés Felipe Gómez Sánchez", "NATURAL", "3157896541", "andres.plomero@servicios.local", "Calle 134 # 19-45, Bogotá", true, false);
            crearSocio("CC", "1032789456", "Claudia Marcela Restrepo Pardo", "NATURAL", "3114567890", "claudia.restrepo@hogar.local", "Carrera 7 # 67-12, Bogotá", true, false);

            // Terceros (Socios): Duales (Clientes y Proveedores simultáneamente - RN-10)
            crearSocio("NIT", "901234567-8", "Construcciones y Remodelaciones del Norte S.A.S.", "EMPRESA", "6017890123", "admin@constructora.local", "Av. Boyacá # 120-15, Bogotá", true, true);
            crearSocio("NIT", "900876543-4", "Ingeniería y Estructuras Metálicas de la Sabana S.A.S.", "EMPRESA", "6018612345", "compras@estructurasabana.local", "Variante Chía - Cota Km 3", true, true);

            log.info("Catálogo enriquecido de ferretería colombiana (15 productos, 10 terceros) cargado con éxito.");
        }
    }

    private Operario crearOActualizarOperario(String username, String rawPassword, String nombreVisible, String email, Perfil perfil) {
        Operario u = operarioRepository.findByUsername(username).orElseGet(() -> {
            Operario op = new Operario();
            op.setUsername(username);
            return op;
        });
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setNombreVisible(nombreVisible);
        u.setEmail(email);
        u.setHabilitado(true);
        u.setPerfiles(new HashSet<>(List.of(perfil)));
        Operario guardado = operarioRepository.save(u);
        log.info("Operario aprovisionado: {} ({})", username, perfil.getDenominacion());
        return guardado;
    }

    private void registrarFichaConStock(Articulo articulo, Fabricante fabricante, Empaque empaque,
                                        MedidaBase medida, String ean, BigDecimal tarifa,
                                        BigDecimal stock, BigDecimal cpp, Operario operario) {
        Ficha ficha = new Ficha();
        ficha.setArticulo(articulo);
        ficha.setFabricante(fabricante);
        ficha.setEmpaque(empaque);
        ficha.setMedidaBase(medida);
        ficha.setCodigoEan(ean);
        ficha.setTarifaPublico(tarifa);
        ficha.setHabilitado(true);
        ficha = fichaRepository.save(ficha);

        nivelRepository.save(new AlmacenNivel(ficha, stock, cpp));

        AlmacenAsiento asiento = new AlmacenAsiento();
        asiento.setFicha(ficha);
        asiento.setTipoAsiento("INICIALIZACION");
        asiento.setVolumen(stock);
        asiento.setCosteIndividualAsiento(cpp);
        asiento.setNivelObtenido(stock);
        asiento.setFolioTipo("REAJUSTE");
        asiento.setFolioId(1L);
        asiento.setCausa("Carga inicial de inventario");
        asiento.setOperario(operario);
        asientoRepository.save(asiento);
    }

    private void crearSocio(String tipoDoc, String nroDoc, String razonSocial, String tipoPersona,
                            String celular, String email, String direccion, boolean esComprador, boolean esSuministrador) {
        Socio s = new Socio();
        s.setTipoIdentificacion(tipoDoc);
        s.setNroIdentificacion(nroDoc);
        s.setNombreLegal(razonSocial);
        s.setTipoPersona(tipoPersona);
        s.setCelular(celular);
        s.setEmail(email);
        s.setUbicacion(direccion);
        s.setEsComprador(esComprador);
        s.setEsSuministrador(esSuministrador);
        s.setHabilitado(true);
        socioRepository.save(s);
    }
}
