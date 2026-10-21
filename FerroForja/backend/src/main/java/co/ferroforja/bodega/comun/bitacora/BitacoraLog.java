package co.ferroforja.bodega.comun.bitacora;

import co.ferroforja.bodega.operarios.entity.Operario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad BitacoraLog: registro inmutable de auditoría básica (RN-11).
 * Almacena exclusivamente: usuario responsable, fecha/hora y descripción.
 */
@Entity
@Table(
    name = "bitacora_log",
    indexes = {
        @Index(name = "idx_bitacora_fecha", columnList = "marca_tiempo")
    }
)
public class BitacoraLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operario_id")
    private Operario operario;

    @Column(name = "marca_tiempo", nullable = false, updatable = false)
    private LocalDateTime marcaTiempo = LocalDateTime.now();

    @Column(name = "modulo", nullable = false, length = 40)
    private String modulo;

    @Column(name = "accion", nullable = false, length = 50)
    private String accion;

    @Column(name = "resena", nullable = false, columnDefinition = "TEXT")
    private String resena;

    public BitacoraLog() {}

    public BitacoraLog(Operario operario, String modulo, String accion, String resena) {
        this.operario = operario;
        this.modulo = modulo;
        this.accion = accion;
        this.resena = resena;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Operario getOperario() { return operario; }
    public void setOperario(Operario operario) { this.operario = operario; }

    public LocalDateTime getMarcaTiempo() { return marcaTiempo; }
    public void setMarcaTiempo(LocalDateTime marcaTiempo) { this.marcaTiempo = marcaTiempo; }

    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getResena() { return resena; }
    public void setResena(String resena) { this.resena = resena; }
}
