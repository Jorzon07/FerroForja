package co.ferroforja.bodega.expendio.entity;

import co.ferroforja.bodega.catalogo.entity.Ficha;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entidad ExpendioDetalle: detalle de productos vendidos, congelando el precio
 * y el costo histórico unitario vigente al momento de la venta (RN-04).
 */
@Entity
@Table(name = "expendio_detalle")
public class ExpendioDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expendio_id", nullable = false)
    private Expendio expendio;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ficha_id", nullable = false)
    private Ficha ficha;

    @Column(name = "volumen", nullable = false, precision = 12, scale = 3)
    private BigDecimal volumen;

    @Column(name = "tarifa_individual", nullable = false, precision = 15, scale = 2)
    private BigDecimal tarifaIndividual;

    @Column(name = "coste_congelado_individual", nullable = false, precision = 15, scale = 4)
    private BigDecimal costeCongeladoIndividual;

    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    public ExpendioDetalle() {}

    public ExpendioDetalle(Ficha ficha, BigDecimal volumen, BigDecimal tarifaIndividual, BigDecimal costeCongeladoIndividual, BigDecimal subtotal) {
        this.ficha = ficha;
        this.volumen = volumen;
        this.tarifaIndividual = tarifaIndividual;
        this.costeCongeladoIndividual = costeCongeladoIndividual;
        this.subtotal = subtotal;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Expendio getExpendio() { return expendio; }
    public void setExpendio(Expendio expendio) { this.expendio = expendio; }

    public Ficha getFicha() { return ficha; }
    public void setFicha(Ficha ficha) { this.ficha = ficha; }

    public BigDecimal getVolumen() { return volumen; }
    public void setVolumen(BigDecimal volumen) { this.volumen = volumen; }

    public BigDecimal getTarifaIndividual() { return tarifaIndividual; }
    public void setTarifaIndividual(BigDecimal tarifaIndividual) { this.tarifaIndividual = tarifaIndividual; }

    public BigDecimal getCosteCongeladoIndividual() { return costeCongeladoIndividual; }
    public void setCosteCongeladoIndividual(BigDecimal costeCongeladoIndividual) { this.costeCongeladoIndividual = costeCongeladoIndividual; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
