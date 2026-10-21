package co.ferroforja.bodega.restituciones.entity;

import co.ferroforja.bodega.socios.entity.Socio;
import co.ferroforja.bodega.expendio.entity.Expendio;
import co.ferroforja.bodega.operarios.entity.Operario;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restitucion")
public class Restitucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "correlativo", nullable = false, unique = true, length = 25)
    private String correlativo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expendio_id")
    private Expendio expendio; // Opcional si es no vinculada

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "comprador_id")
    private Socio comprador;

    @Column(name = "tipo_restitucion", nullable = false, length = 20)
    private String tipoRestitucion; // VINCULADA, NO_VINCULADA

    @Column(name = "marca_tiempo", nullable = false, updatable = false)
    private LocalDateTime marcaTiempo = LocalDateTime.now();

    @Column(name = "causa", nullable = false, length = 255)
    private String causa;

    @Column(name = "situacion", nullable = false, length = 20)
    private String situacion = "APROBADA";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operario_id", nullable = false)
    private Operario operario;

    @OneToMany(mappedBy = "restitucion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RestitucionDetalle> detalles = new ArrayList<>();

    public Restitucion() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCorrelativo() { return correlativo; }
    public void setCorrelativo(String correlativo) { this.correlativo = correlativo; }

    public Expendio getExpendio() { return expendio; }
    public void setExpendio(Expendio expendio) { this.expendio = expendio; }

    public Socio getComprador() { return comprador; }
    public void setComprador(Socio comprador) { this.comprador = comprador; }

    public String getTipoRestitucion() { return tipoRestitucion; }
    public void setTipoRestitucion(String tipoRestitucion) { this.tipoRestitucion = tipoRestitucion; }

    public LocalDateTime getMarcaTiempo() { return marcaTiempo; }
    public void setMarcaTiempo(LocalDateTime marcaTiempo) { this.marcaTiempo = marcaTiempo; }

    public String getCausa() { return causa; }
    public void setCausa(String causa) { this.causa = causa; }

    public String getSituacion() { return situacion; }
    public void setSituacion(String situacion) { this.situacion = situacion; }

    public Operario getOperario() { return operario; }
    public void setOperario(Operario operario) { this.operario = operario; }

    public List<RestitucionDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<RestitucionDetalle> detalles) { this.detalles = detalles; }

    public void addDetalle(RestitucionDetalle detalle) {
        detalles.add(detalle);
        detalle.setRestitucion(this);
    }
}
