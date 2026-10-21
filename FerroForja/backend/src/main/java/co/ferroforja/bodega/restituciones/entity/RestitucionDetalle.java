package co.ferroforja.bodega.restituciones.entity;

import co.ferroforja.bodega.catalogo.entity.Ficha;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "restitucion_detalle")
public class RestitucionDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restitucion_id", nullable = false)
    private Restitucion restitucion;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ficha_id", nullable = false)
    private Ficha ficha;

    @Column(name = "volumen", nullable = false, precision = 12, scale = 3)
    private BigDecimal volumen;

    @Column(name = "coste_individual_aplicado", nullable = false, precision = 15, scale = 4)
    private BigDecimal costeIndividualAplicado;

    public RestitucionDetalle() {}

    public RestitucionDetalle(Ficha ficha, BigDecimal volumen, BigDecimal costeIndividualAplicado) {
        this.ficha = ficha;
        this.volumen = volumen;
        this.costeIndividualAplicado = costeIndividualAplicado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Restitucion getRestitucion() { return restitucion; }
    public void setRestitucion(Restitucion restitucion) { this.restitucion = restitucion; }

    public Ficha getFicha() { return ficha; }
    public void setFicha(Ficha ficha) { this.ficha = ficha; }

    public BigDecimal getVolumen() { return volumen; }
    public void setVolumen(BigDecimal volumen) { this.volumen = volumen; }

    public BigDecimal getCosteIndividualAplicado() { return costeIndividualAplicado; }
    public void setCosteIndividualAplicado(BigDecimal costeIndividualAplicado) { this.costeIndividualAplicado = costeIndividualAplicado; }
}
