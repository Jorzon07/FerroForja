package co.ferroforja.bodega.almacen.entity;

import co.ferroforja.bodega.catalogo.entity.Ficha;
import co.ferroforja.bodega.operarios.entity.Operario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad ReajusteAlmacen: operaciones manuales autorizadas exclusivamente
 * por el Administrador con motivo documentado obligatorio (RN-08).
 */
@Entity
@Table(name = "reajuste_almacen")
public class ReajusteAlmacen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "correlativo", nullable = false, unique = true, length = 25)
    private String correlativo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ficha_id", nullable = false)
    private Ficha ficha;

    @Column(name = "tipo_reajuste", nullable = false, length = 20)
    private String tipoReajuste; // INCREMENTO, DISMINUCION

    @Column(name = "volumen", nullable = false, precision = 12, scale = 3)
    private BigDecimal volumen;

    @Column(name = "coste_individual_ficha", nullable = false, precision = 15, scale = 4)
    private BigDecimal costeIndividualFicha;

    @Column(name = "causa", nullable = false, length = 255)
    private String causa;

    @Column(name = "marca_tiempo", nullable = false, updatable = false)
    private LocalDateTime marcaTiempo = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "operario_id", nullable = false)
    private Operario operario;

    public ReajusteAlmacen() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCorrelativo() { return correlativo; }
    public void setCorrelativo(String correlativo) { this.correlativo = correlativo; }

    public Ficha getFicha() { return ficha; }
    public void setFicha(Ficha ficha) { this.ficha = ficha; }

    public String getTipoReajuste() { return tipoReajuste; }
    public void setTipoReajuste(String tipoReajuste) { this.tipoReajuste = tipoReajuste; }

    public BigDecimal getVolumen() { return volumen; }
    public void setVolumen(BigDecimal volumen) { this.volumen = volumen; }

    public BigDecimal getCosteIndividualFicha() { return costeIndividualFicha; }
    public void setCosteIndividualFicha(BigDecimal costeIndividualFicha) { this.costeIndividualFicha = costeIndividualFicha; }

    public String getCausa() { return causa; }
    public void setCausa(String causa) { this.causa = causa; }

    public LocalDateTime getMarcaTiempo() { return marcaTiempo; }
    public void setMarcaTiempo(LocalDateTime marcaTiempo) { this.marcaTiempo = marcaTiempo; }

    public Operario getOperario() { return operario; }
    public void setOperario(Operario operario) { this.operario = operario; }
}
