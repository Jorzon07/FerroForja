package co.ferroforja.bodega.abastecimiento.entity;

import co.ferroforja.bodega.catalogo.entity.Ficha;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "abastecimiento_detalle")
public class AbastecimientoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "abastecimiento_id", nullable = false)
    private Abastecimiento abastecimiento;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ficha_id", nullable = false)
    private Ficha ficha;

    @Column(name = "volumen", nullable = false, precision = 12, scale = 3)
    private BigDecimal volumen;

    @Column(name = "coste_individual", nullable = false, precision = 15, scale = 4)
    private BigDecimal costeIndividual;

    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    public AbastecimientoDetalle() {}

    public AbastecimientoDetalle(Ficha ficha, BigDecimal volumen, BigDecimal costeIndividual, BigDecimal subtotal) {
        this.ficha = ficha;
        this.volumen = volumen;
        this.costeIndividual = costeIndividual;
        this.subtotal = subtotal;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Abastecimiento getAbastecimiento() { return abastecimiento; }
    public void setAbastecimiento(Abastecimiento abastecimiento) { this.abastecimiento = abastecimiento; }

    public Ficha getFicha() { return ficha; }
    public void setFicha(Ficha ficha) { this.ficha = ficha; }

    public BigDecimal getVolumen() { return volumen; }
    public void setVolumen(BigDecimal volumen) { this.volumen = volumen; }

    public BigDecimal getCosteIndividual() { return costeIndividual; }
    public void setCosteIndividual(BigDecimal costeIndividual) { this.costeIndividual = costeIndividual; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
