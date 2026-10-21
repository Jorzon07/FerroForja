package co.ferroforja.bodega.catalogo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad Referencia: representa el artículo vendible en catálogo
 * resultante de la combinación de Producto, Marca y Presentación.
 */
@Entity
@Table(
    name = "ficha",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_fch_codigo_ean", columnNames = {"codigo_ean"}),
        @UniqueConstraint(name = "uk_fch_prod_fabricante_emp", columnNames = {"articulo_id", "fabricante_id", "empaque_id"})
    }
)
public class Ficha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "articulo_id", nullable = false)
    private Articulo articulo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "fabricante_id", nullable = false)
    private Fabricante fabricante;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "empaque_id", nullable = false)
    private Empaque empaque;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "medida_base_id", nullable = false)
    private MedidaBase medidaBase;

    @Column(name = "codigo_ean", nullable = false, length = 50)
    private String codigoEan;

    @Column(name = "tarifa_publico", nullable = false, precision = 15, scale = 2)
    private BigDecimal tarifaPublico;

    @Column(name = "habilitado", nullable = false)
    private boolean habilitado = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    public Ficha() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Articulo getArticulo() { return articulo; }
    public void setArticulo(Articulo articulo) { this.articulo = articulo; }

    public Fabricante getFabricante() { return fabricante; }
    public void setFabricante(Fabricante fabricante) { this.fabricante = fabricante; }

    public Empaque getEmpaque() { return empaque; }
    public void setEmpaque(Empaque empaque) { this.empaque = empaque; }

    public MedidaBase getMedidaBase() { return medidaBase; }
    public void setMedidaBase(MedidaBase medidaBase) { this.medidaBase = medidaBase; }

    public String getCodigoEan() { return codigoEan; }
    public void setCodigoEan(String codigoEan) { this.codigoEan = codigoEan; }

    public BigDecimal getTarifaPublico() { return tarifaPublico; }
    public void setTarifaPublico(BigDecimal tarifaPublico) { this.tarifaPublico = tarifaPublico; }

    public boolean isHabilitado() { return habilitado; }
    public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public String getNombreVisible() {
        return articulo.getDenominacion() + " (" + fabricante.getDenominacion() + " - " + empaque.getDenominacion() + ")";
    }
}
