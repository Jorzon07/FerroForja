package co.ferroforja.bodega.almacen.entity;

import co.ferroforja.bodega.catalogo.entity.Ficha;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad de Saldo de Inventario: mantiene el saldo físico disponible actual
 * y el Costo Promedio Ponderado (CPP) por referencia (RN-01, RN-04).
 */
@Entity
@Table(name = "almacen_nivel")
public class AlmacenNivel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ficha_id", nullable = false, unique = true)
    private Ficha ficha;

    @Column(name = "volumen_libre", nullable = false, precision = 12, scale = 3)
    private BigDecimal volumenLibre = BigDecimal.ZERO;

    @Column(name = "coste_medio_ponderado", nullable = false, precision = 15, scale = 4)
    private BigDecimal costeMedioPonderado = BigDecimal.ZERO;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn = LocalDateTime.now();

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    public AlmacenNivel() {}

    public AlmacenNivel(Ficha ficha, BigDecimal volumenLibre, BigDecimal costeMedioPonderado) {
        this.ficha = ficha;
        this.volumenLibre = volumenLibre;
        this.costeMedioPonderado = costeMedioPonderado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Ficha getFicha() { return ficha; }
    public void setFicha(Ficha ficha) { this.ficha = ficha; }

    public BigDecimal getVolumenLibre() { return volumenLibre; }
    public void setVolumenLibre(BigDecimal volumenLibre) { this.volumenLibre = volumenLibre; }

    public BigDecimal getCosteMedioPonderado() { return costeMedioPonderado; }
    public void setCosteMedioPonderado(BigDecimal costeMedioPonderado) { this.costeMedioPonderado = costeMedioPonderado; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    @PreUpdate
    public void preUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}
