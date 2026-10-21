package co.ferroforja.bodega.abastecimiento.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AbastecimientoResponse {
    private Long id;
    private String correlativo;
    private Long suministradorId;
    private String suministradorNombreLegal;
    private String suministradorFolio;
    private LocalDateTime marcaTiempo;
    private BigDecimal total;
    private String situacion;
    private String causaAnulacion;
    private String operarioDenominacion;
    private List<AbastecimientoDetalleResponse> detalles;

    public AbastecimientoResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCorrelativo() { return correlativo; }
    public void setCorrelativo(String correlativo) { this.correlativo = correlativo; }
    public Long getSuministradorId() { return suministradorId; }
    public void setSuministradorId(Long suministradorId) { this.suministradorId = suministradorId; }
    public String getSuministradorNombreLegal() { return suministradorNombreLegal; }
    public void setSuministradorNombreLegal(String suministradorNombreLegal) { this.suministradorNombreLegal = suministradorNombreLegal; }
    public String getSuministradorFolio() { return suministradorFolio; }
    public void setSuministradorFolio(String suministradorFolio) { this.suministradorFolio = suministradorFolio; }
    public LocalDateTime getMarcaTiempo() { return marcaTiempo; }
    public void setMarcaTiempo(LocalDateTime marcaTiempo) { this.marcaTiempo = marcaTiempo; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getSituacion() { return situacion; }
    public void setSituacion(String situacion) { this.situacion = situacion; }
    public String getCausaAnulacion() { return causaAnulacion; }
    public void setCausaAnulacion(String causaAnulacion) { this.causaAnulacion = causaAnulacion; }
    public String getOperarioDenominacion() { return operarioDenominacion; }
    public void setOperarioDenominacion(String operarioDenominacion) { this.operarioDenominacion = operarioDenominacion; }
    public List<AbastecimientoDetalleResponse> getDetalles() { return detalles; }
    public void setDetalles(List<AbastecimientoDetalleResponse> detalles) { this.detalles = detalles; }
}
