package co.ferroforja.bodega.catalogo.dto.response;

import java.math.BigDecimal;

public class FichaResponse {
    private Long id;
    private Long articuloId;
    private String articuloDenominacion;
    private Long fabricanteId;
    private String fabricanteDenominacion;
    private Long empaqueId;
    private String empaqueDenominacion;
    private Long medidaBaseId;
    private String medidaBaseClave;
    private String codigoEan;
    private BigDecimal tarifaPublico;
    private boolean habilitado;
    private BigDecimal existenciaLibre;
    private BigDecimal costeMedio;

    public FichaResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getArticuloId() { return articuloId; }
    public void setArticuloId(Long articuloId) { this.articuloId = articuloId; }
    public String getArticuloDenominacion() { return articuloDenominacion; }
    public void setArticuloDenominacion(String articuloDenominacion) { this.articuloDenominacion = articuloDenominacion; }
    public Long getFabricanteId() { return fabricanteId; }
    public void setFabricanteId(Long fabricanteId) { this.fabricanteId = fabricanteId; }
    public String getFabricanteDenominacion() { return fabricanteDenominacion; }
    public void setFabricanteDenominacion(String fabricanteDenominacion) { this.fabricanteDenominacion = fabricanteDenominacion; }
    public Long getEmpaqueId() { return empaqueId; }
    public void setEmpaqueId(Long empaqueId) { this.empaqueId = empaqueId; }
    public String getEmpaqueDenominacion() { return empaqueDenominacion; }
    public void setEmpaqueDenominacion(String empaqueDenominacion) { this.empaqueDenominacion = empaqueDenominacion; }
    public Long getMedidaBaseId() { return medidaBaseId; }
    public void setMedidaBaseId(Long medidaBaseId) { this.medidaBaseId = medidaBaseId; }
    public String getMedidaBaseClave() { return medidaBaseClave; }
    public void setMedidaBaseClave(String medidaBaseClave) { this.medidaBaseClave = medidaBaseClave; }
    public String getCodigoEan() { return codigoEan; }
    public void setCodigoEan(String codigoEan) { this.codigoEan = codigoEan; }
    public BigDecimal getTarifaPublico() { return tarifaPublico; }
    public void setTarifaPublico(BigDecimal tarifaPublico) { this.tarifaPublico = tarifaPublico; }
    public boolean isHabilitado() { return habilitado; }
    public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }
    public BigDecimal getExistenciaLibre() { return existenciaLibre; }
    public void setExistenciaLibre(BigDecimal existenciaLibre) { this.existenciaLibre = existenciaLibre; }
    public BigDecimal getCosteMedio() { return costeMedio; }
    public void setCosteMedio(BigDecimal costeMedio) { this.costeMedio = costeMedio; }
}
