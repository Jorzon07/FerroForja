package co.ferroforja.bodega.catalogo.service;

import co.ferroforja.bodega.catalogo.dto.request.ModificarTarifaRequest;
import co.ferroforja.bodega.catalogo.dto.request.GenerarFichaRequest;
import co.ferroforja.bodega.catalogo.dto.response.FichaResponse;
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
import co.ferroforja.bodega.almacen.repository.AlmacenAsientoRepository;
import co.ferroforja.bodega.almacen.repository.AlmacenNivelRepository;
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

@Service
public class CatalogoService {

    private final FichaRepository fichaRepository;
    private final ArticuloRepository articuloRepository;
    private final FabricanteRepository fabricanteRepository;
    private final EmpaqueRepository empaqueRepository;
    private final MedidaBaseRepository medidaBaseRepository;
    private final AlmacenNivelRepository nivelRepository;
    private final AlmacenAsientoRepository asientoRepository;
    private final OperarioRepository operarioRepository;
    private final BitacoraService bitacoraService;

    public CatalogoService(FichaRepository fichaRepository,
                          ArticuloRepository articuloRepository,
                          FabricanteRepository fabricanteRepository,
                          EmpaqueRepository empaqueRepository,
                          MedidaBaseRepository medidaBaseRepository,
                          AlmacenNivelRepository nivelRepository,
                          AlmacenAsientoRepository asientoRepository,
                          OperarioRepository operarioRepository,
                          BitacoraService bitacoraService) {
        this.fichaRepository = fichaRepository;
        this.articuloRepository = articuloRepository;
        this.fabricanteRepository = fabricanteRepository;
        this.empaqueRepository = empaqueRepository;
        this.medidaBaseRepository = medidaBaseRepository;
        this.nivelRepository = nivelRepository;
        this.asientoRepository = asientoRepository;
        this.operarioRepository = operarioRepository;
        this.bitacoraService = bitacoraService;
    }

    @Transactional(readOnly = true)
    public Page<FichaResponse> enumerarFichas(String search, Boolean habilitado, Pageable pageable) {
        return fichaRepository.searchFichas(search, habilitado, pageable)
                .map(this::convertirARespuesta);
    }

    @Transactional(readOnly = true)
    public FichaResponse recuperarPorId(Long id) {
        Ficha fch = fichaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Referencia no encontrada con ID: " + id));
        return convertirARespuesta(fch);
    }

    @Transactional(readOnly = true)
    public FichaResponse recuperarPorCodigoEan(String codigoEan) {
        Ficha fch = fichaRepository.findByCodigoEan(codigoEan)
                .orElseThrow(() -> new ResourceNotFoundException("Referencia no encontrada con código de barras: " + codigoEan));
        return convertirARespuesta(fch);
    }

    @Transactional
    public FichaResponse generarFicha(GenerarFichaRequest request, String username) {
        if (fichaRepository.existsByCodigoEan(request.getCodigoEan())) {
            throw new BusinessException("Ya existe una referencia con el código de barras: " + request.getCodigoEan());
        }

        // Resolver Producto
        Articulo articulo;
        if (request.getArticuloId() != null) {
            articulo = articuloRepository.findById(request.getArticuloId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + request.getArticuloId()));
        } else if (request.getArticuloDenominacion() != null && !request.getArticuloDenominacion().trim().isEmpty()) {
            String pDenominacion = request.getArticuloDenominacion().trim();
            articulo = articuloRepository.findByDenominacion(pDenominacion)
                    .orElseGet(() -> articuloRepository.save(new Articulo(null, pDenominacion, pDenominacion, true)));
        } else {
            throw new BusinessException("Debe seleccionar o ingresar el nombre del producto");
        }

        // Resolver Marca
        Fabricante fabricante;
        if (request.getFabricanteId() != null) {
            fabricante = fabricanteRepository.findById(request.getFabricanteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con ID: " + request.getFabricanteId()));
        } else if (request.getFabricanteDenominacion() != null && !request.getFabricanteDenominacion().trim().isEmpty()) {
            String mDenominacion = request.getFabricanteDenominacion().trim();
            fabricante = fabricanteRepository.findByDenominacion(mDenominacion)
                    .orElseGet(() -> fabricanteRepository.save(new Fabricante(null, mDenominacion, true)));
        } else {
            fabricante = fabricanteRepository.findByDenominacion("Generico")
                    .orElseGet(() -> fabricanteRepository.save(new Fabricante(null, "Generico", true)));
        }

        // Resolver Presentación
        Empaque empaque;
        if (request.getEmpaqueId() != null) {
            empaque = empaqueRepository.findById(request.getEmpaqueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Presentación no encontrada con ID: " + request.getEmpaqueId()));
        } else if (request.getEmpaqueDenominacion() != null && !request.getEmpaqueDenominacion().trim().isEmpty()) {
            String empDenominacion = request.getEmpaqueDenominacion().trim();
            empaque = empaqueRepository.findByDenominacion(empDenominacion)
                    .orElseGet(() -> empaqueRepository.save(new Empaque(null, empDenominacion, true)));
        } else {
            empaque = empaqueRepository.findByDenominacion("Unidad Suelta")
                    .orElseGet(() -> empaqueRepository.save(new Empaque(null, "Unidad Suelta", true)));
        }

        // Resolver Unidad de Medida
        MedidaBase medida;
        if (request.getMedidaBaseId() != null) {
            medida = medidaBaseRepository.findById(request.getMedidaBaseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unidad de medida no encontrada con ID: " + request.getMedidaBaseId()));
        } else if (request.getMedidaBaseClave() != null && !request.getMedidaBaseClave().trim().isEmpty()) {
            String uCode = request.getMedidaBaseClave().trim().toUpperCase();
            medida = medidaBaseRepository.findByClave(uCode)
                    .orElseGet(() -> medidaBaseRepository.save(new MedidaBase(null, uCode, uCode)));
        } else {
            medida = medidaBaseRepository.findByClave("UND")
                    .orElseGet(() -> medidaBaseRepository.save(new MedidaBase(null, "UND", "Unidad")));
        }

        if (fichaRepository.existsByArticuloIdAndFabricanteIdAndEmpaqueId(
                articulo.getId(), fabricante.getId(), empaque.getId())) {
            throw new BusinessException("Ya existe una referencia registrada con la combinación de Producto: '" +
                    articulo.getDenominacion() + "', Marca: '" + fabricante.getDenominacion() + "' y Presentación: '" + empaque.getDenominacion() + "'");
        }

        Ficha fch = new Ficha();
        fch.setArticulo(articulo);
        fch.setFabricante(fabricante);
        fch.setEmpaque(empaque);
        fch.setMedidaBase(medida);
        fch.setCodigoEan(request.getCodigoEan().trim());
        fch.setTarifaPublico(request.getTarifaPublico());
        fch.setHabilitado(true);

        Ficha guardada = fichaRepository.save(fch);

        // Inicializar saldo en 0.000 y CPP en 0.0000
        AlmacenNivel nivel = new AlmacenNivel(guardada, BigDecimal.ZERO, BigDecimal.ZERO);
        nivelRepository.save(nivel);

        Operario operario = operarioRepository.findByUsername(username).orElse(null);
        bitacoraService.asentarEvento(operario, "CATALOGO", "GENERAR_FICHA",
                "Creación de referencia " + guardada.getCodigoEan() + " - " + guardada.getNombreVisible());

        return convertirARespuesta(guardada);
    }

    @Transactional
    public FichaResponse conmutarSituacion(Long id, String username) {
        Ficha fch = fichaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Referencia no encontrada con ID: " + id));

        boolean nuevoSituacion = !fch.isHabilitado();
        fch.setHabilitado(nuevoSituacion);
        fichaRepository.save(fch);

        Operario operario = operarioRepository.findByUsername(username).orElse(null);
        bitacoraService.asentarEvento(operario, "CATALOGO", nuevoSituacion ? "HABILITAR_FICHA" : "DESHABILITAR_FICHA",
                (nuevoSituacion ? "Activación" : "Desactivación") + " de referencia " + fch.getCodigoEan());

        return convertirARespuesta(fch);
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Object> recuperarBases() {
        return java.util.Map.of(
                "articulos", articuloRepository.findAll(),
                "fabricantes", fabricanteRepository.findAll(),
                "empaques", empaqueRepository.findAll(),
                "medidasBase", medidaBaseRepository.findAll()
        );
    }

    @Transactional
    public FichaResponse modificarTarifa(Long id, ModificarTarifaRequest request, String username) {
        Ficha fch = fichaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Referencia no encontrada con ID: " + id));

        BigDecimal tarifaAnterior = fch.getTarifaPublico();
        fch.setTarifaPublico(request.getNuevoTarifa());
        fichaRepository.save(fch);

        Operario operario = operarioRepository.findByUsername(username).orElse(null);
        bitacoraService.asentarEvento(operario, "CATALOGO", "MODIFICAR_TARIFA",
                "Cambio de precio para " + fch.getCodigoEan() + ": $" + tarifaAnterior + " -> $" + request.getNuevoTarifa() +
                        (request.getCausa() != null ? ". Motivo: " + request.getCausa() : ""));

        return convertirARespuesta(fch);
    }

    @Transactional
    public void deshabilitarFicha(Long id, String username) {
        Ficha fch = fichaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Referencia no encontrada con ID: " + id));

        // Regla RN-02: si tiene movimientos o transacciones, no se elimina físicamente; se desactiva
        boolean tieneHistorial = asientoRepository.existsByFichaId(id);

        if (tieneHistorial) {
            fch.setHabilitado(false);
            fichaRepository.save(fch);
        } else {
            // Si no tiene ningún historial se puede remover el saldo y la referencia
            nivelRepository.findByFichaId(id).ifPresent(nivelRepository::delete);
            fichaRepository.delete(fch);
        }

        Operario operario = operarioRepository.findByUsername(username).orElse(null);
        bitacoraService.asentarEvento(operario, "CATALOGO", "DESHABILITAR_FICHA",
                "Desactivación lógica de referencia " + fch.getCodigoEan());
    }

    public Ficha recuperarEntidadPorId(Long id) {
        return fichaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Referencia no encontrada con ID: " + id));
    }

    private FichaResponse convertirARespuesta(Ficha r) {
        FichaResponse res = new FichaResponse();
        res.setId(r.getId());
        res.setArticuloId(r.getArticulo().getId());
        res.setArticuloDenominacion(r.getArticulo().getDenominacion());
        res.setFabricanteId(r.getFabricante().getId());
        res.setFabricanteDenominacion(r.getFabricante().getDenominacion());
        res.setEmpaqueId(r.getEmpaque().getId());
        res.setEmpaqueDenominacion(r.getEmpaque().getDenominacion());
        res.setMedidaBaseId(r.getMedidaBase().getId());
        res.setMedidaBaseClave(r.getMedidaBase().getClave());
        res.setCodigoEan(r.getCodigoEan());
        res.setTarifaPublico(r.getTarifaPublico());
        res.setHabilitado(r.isHabilitado());

        nivelRepository.findByFichaId(r.getId()).ifPresent(nivel -> {
            res.setExistenciaLibre(nivel.getVolumenLibre());
            res.setCosteMedio(nivel.getCosteMedioPonderado());
        });

        return res;
    }
}
