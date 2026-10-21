package co.ferroforja.bodega.abastecimiento.entity;

import co.ferroforja.bodega.socios.entity.Socio;
import co.ferroforja.bodega.operarios.entity.Operario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "abastecimiento")
public class Abastecimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "correlativo", nullable = false, unique = true, length = 25)
    private String correlativo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "suministrador_id", nullable = false)
    private Socio suministrador;

    @Column(name = "marca_tiempo", nullable = false, updatable = false)
    private LocalDateTime marcaTiempo = LocalDateTime.now();

    @Column(name = "total", nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    @Column(name = "situacion", nullable = false, length = 20)
    private String situacion = "RECIBIDA"; // RECIBIDA, CANCELADA

    @Column(name = "causa_anulacion", length = 255)
    private String causaAnulacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operario_id", nullable = false)
    private Operario operario;

    @OneToMany(mappedBy = "abastecimiento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AbastecimientoDetalle> detalles = new ArrayList<>();

    public Abastecimiento() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCorrelativo() { return correlativo; }
    public void setCorrelativo(String correlativo) { this.correlativo = correlativo; }

    public Socio getSuministrador() { return suministrador; }
    public void setSuministrador(Socio suministrador) { this.suministrador = suministrador; }

    public LocalDateTime getMarcaTiempo() { return marcaTiempo; }
    public void setMarcaTiempo(LocalDateTime marcaTiempo) { this.marcaTiempo = marcaTiempo; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getSituacion() { return situacion; }
    public void setSituacion(String situacion) { this.situacion = situacion; }

    public String getCausaAnulacion() { return causaAnulacion; }
    public void setCausaAnulacion(String causaAnulacion) { this.causaAnulacion = causaAnulacion; }

    public Operario getOperario() { return operario; }
    public void setOperario(Operario operario) { this.operario = operario; }

    public List<AbastecimientoDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<AbastecimientoDetalle> detalles) { this.detalles = detalles; }

    public void addDetalle(AbastecimientoDetalle detalle) {
        detalles.add(detalle);
        detalle.setAbastecimiento(this);
    }
}
