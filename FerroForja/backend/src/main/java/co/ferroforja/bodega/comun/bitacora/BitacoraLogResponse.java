package co.ferroforja.bodega.comun.bitacora;

import java.time.LocalDateTime;

public class BitacoraLogResponse {
    private Long id;
    private String operarioDenominacion;
    private LocalDateTime marcaTiempo;
    private String modulo;
    private String accion;
    private String resena;

    public BitacoraLogResponse() {}

    public BitacoraLogResponse(Long id, String operarioDenominacion, LocalDateTime marcaTiempo, String modulo, String accion, String resena) {
        this.id = id;
        this.operarioDenominacion = operarioDenominacion;
        this.marcaTiempo = marcaTiempo;
        this.modulo = modulo;
        this.accion = accion;
        this.resena = resena;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOperarioDenominacion() { return operarioDenominacion; }
    public void setOperarioDenominacion(String operarioDenominacion) { this.operarioDenominacion = operarioDenominacion; }
    public LocalDateTime getMarcaTiempo() { return marcaTiempo; }
    public void setMarcaTiempo(LocalDateTime marcaTiempo) { this.marcaTiempo = marcaTiempo; }
    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public String getResena() { return resena; }
    public void setResena(String resena) { this.resena = resena; }
}
