package co.ferroforja.bodega.expendio.entity;

import co.ferroforja.bodega.socios.entity.Socio;
import co.ferroforja.bodega.operarios.entity.Operario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expendio")
public class Expendio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "correlativo", nullable = false, unique = true, length = 25)
    private String correlativo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "comprador_id", nullable = false)
    private Socio comprador;

    @Column(name = "marca_tiempo", nullable = false, updatable = false)
    private LocalDateTime marcaTiempo = LocalDateTime.now();

    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "total", nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    @Column(name = "importe_abonado_efectivo", nullable = false, precision = 15, scale = 2)
    private BigDecimal importeAbonadoEfectivo;

    @Column(name = "vuelto_entregado", nullable = false, precision = 15, scale = 2)
    private BigDecimal vueltoEntregado = BigDecimal.ZERO;

    @Column(name = "situacion", nullable = false, length = 20)
    private String situacion = "COMPLETADA"; // COMPLETADA, CANCELADA

    @Column(name = "causa_anulacion", length = 255)
    private String causaAnulacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operario_id", nullable = false)
    private Operario operario;

    @OneToMany(mappedBy = "expendio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ExpendioDetalle> detalles = new ArrayList<>();

    public Expendio() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCorrelativo() { return correlativo; }
    public void setCorrelativo(String correlativo) { this.correlativo = correlativo; }

    public Socio getComprador() { return comprador; }
    public void setComprador(Socio comprador) { this.comprador = comprador; }

    public LocalDateTime getMarcaTiempo() { return marcaTiempo; }
    public void setMarcaTiempo(LocalDateTime marcaTiempo) { this.marcaTiempo = marcaTiempo; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public BigDecimal getImporteAbonadoEfectivo() { return importeAbonadoEfectivo; }
    public void setImporteAbonadoEfectivo(BigDecimal importeAbonadoEfectivo) { this.importeAbonadoEfectivo = importeAbonadoEfectivo; }

    public BigDecimal getVueltoEntregado() { return vueltoEntregado; }
    public void setVueltoEntregado(BigDecimal vueltoEntregado) { this.vueltoEntregado = vueltoEntregado; }

    public String getSituacion() { return situacion; }
    public void setSituacion(String situacion) { this.situacion = situacion; }

    public String getCausaAnulacion() { return causaAnulacion; }
    public void setCausaAnulacion(String causaAnulacion) { this.causaAnulacion = causaAnulacion; }

    public Operario getOperario() { return operario; }
    public void setOperario(Operario operario) { this.operario = operario; }

    public List<ExpendioDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<ExpendioDetalle> detalles) { this.detalles = detalles; }

    public void addDetalle(ExpendioDetalle detalle) {
        detalles.add(detalle);
        detalle.setExpendio(this);
    }
}
