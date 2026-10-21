package co.ferroforja.bodega.restituciones.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class RestitucionResponse {
    private Long id;
    private String correlativo;
    private Long expendioId;
    private String expendioCorrelativo;
    private Long compradorId;
    private String compradorDenominacion;
    private String tipoRestitucion;
    private LocalDateTime marcaTiempo;
    private String causa;
    private String situacion;
    private String operarioDenominacion;
    private List<RestitucionDetalleResponse> detalles;

    public RestitucionResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCorrelativo() { return correlativo; }
    public void setCorrelativo(String correlativo) { this.correlativo = correlativo; }
    public Long getExpendioId() { return expendioId; }
    public void setExpendioId(Long expendioId) { this.expendioId = expendioId; }
    public String getExpendioCorrelativo() { return expendioCorrelativo; }
    public void setExpendioCorrelativo(String expendioCorrelativo) { this.expendioCorrelativo = expendioCorrelativo; }
    public Long getCompradorId() { return compradorId; }
    public void setCompradorId(Long compradorId) { this.compradorId = compradorId; }
    public String getCompradorDenominacion() { return compradorDenominacion; }
    public void setCompradorDenominacion(String compradorDenominacion) { this.compradorDenominacion = compradorDenominacion; }
    public String getTipoRestitucion() { return tipoRestitucion; }
    public void setTipoRestitucion(String tipoRestitucion) { this.tipoRestitucion = tipoRestitucion; }
    public LocalDateTime getMarcaTiempo() { return marcaTiempo; }
    public void setMarcaTiempo(LocalDateTime marcaTiempo) { this.marcaTiempo = marcaTiempo; }
    public String getCausa() { return causa; }
    public void setCausa(String causa) { this.causa = causa; }
    public String getSituacion() { return situacion; }
    public void setSituacion(String situacion) { this.situacion = situacion; }
    public String getOperarioDenominacion() { return operarioDenominacion; }
    public void setOperarioDenominacion(String operarioDenominacion) { this.operarioDenominacion = operarioDenominacion; }
    public List<RestitucionDetalleResponse> getDetalles() { return detalles; }
    public void setDetalles(List<RestitucionDetalleResponse> detalles) { this.detalles = detalles; }
}
