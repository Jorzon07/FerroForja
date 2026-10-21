package co.ferroforja.bodega.almacen.entity;

import co.ferroforja.bodega.catalogo.entity.Ficha;
import co.ferroforja.bodega.operarios.entity.Operario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad AlmacenAsiento: registro inmutable del Kárdex de inventario.
 */
@Entity
@Table(
    name = "almacen_asiento",
    indexes = {
        @Index(name = "idx_asi_fch_fecha", columnList = "ficha_id, marca_tiempo"),
        @Index(name = "idx_asi_doc", columnList = "folio_tipo, folio_id")
    }
)
public class AlmacenAsiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ficha_id", nullable = false)
    private Ficha ficha;

    @Column(name = "tipo_asiento", nullable = false, length = 30)
    private String tipoAsiento; // INICIALIZACION, COMPRA, VENTA, RESTITUCION_COMPRADOR, REAJUSTE_ENTRADA, REAJUSTE_SALIDA, ANULACION_EXPENDIO, ANULACION_ABASTECIMIENTO

    @Column(name = "volumen", nullable = false, precision = 12, scale = 3)
    private BigDecimal volumen;

    @Column(name = "coste_individual_asiento", nullable = false, precision = 15, scale = 4)
    private BigDecimal costeIndividualAsiento;

    @Column(name = "nivel_obtenido", nullable = false, precision = 12, scale = 3)
    private BigDecimal nivelObtenido;

    @Column(name = "folio_tipo", nullable = false, length = 20)
    private String folioTipo; // VENTA, COMPRA, DEVOLUCION, AJUSTE

    @Column(name = "folio_id", nullable = false)
    private Long folioId;

    @Column(name = "causa", length = 255)
    private String causa;

    @Column(name = "marca_tiempo", nullable = false, updatable = false)
    private LocalDateTime marcaTiempo = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operario_id", nullable = false)
    private Operario operario;

    public AlmacenAsiento() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Ficha getFicha() { return ficha; }
    public void setFicha(Ficha ficha) { this.ficha = ficha; }

    public String getTipoAsiento() { return tipoAsiento; }
    public void setTipoAsiento(String tipoAsiento) { this.tipoAsiento = tipoAsiento; }

    public BigDecimal getVolumen() { return volumen; }
    public void setVolumen(BigDecimal volumen) { this.volumen = volumen; }

    public BigDecimal getCosteIndividualAsiento() { return costeIndividualAsiento; }
    public void setCosteIndividualAsiento(BigDecimal costeIndividualAsiento) { this.costeIndividualAsiento = costeIndividualAsiento; }

    public BigDecimal getNivelObtenido() { return nivelObtenido; }
    public void setNivelObtenido(BigDecimal nivelObtenido) { this.nivelObtenido = nivelObtenido; }

    public String getFolioTipo() { return folioTipo; }
    public void setFolioTipo(String folioTipo) { this.folioTipo = folioTipo; }

    public Long getFolioId() { return folioId; }
    public void setFolioId(Long folioId) { this.folioId = folioId; }

    public String getCausa() { return causa; }
    public void setCausa(String causa) { this.causa = causa; }

    public LocalDateTime getMarcaTiempo() { return marcaTiempo; }
    public void setMarcaTiempo(LocalDateTime marcaTiempo) { this.marcaTiempo = marcaTiempo; }

    public Operario getOperario() { return operario; }
    public void setOperario(Operario operario) { this.operario = operario; }
}
