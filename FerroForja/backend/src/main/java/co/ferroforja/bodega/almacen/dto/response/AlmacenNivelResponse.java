package co.ferroforja.bodega.almacen.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AlmacenNivelResponse {
    private Long id;
    private Long fichaId;
    private String codigoEan;
    private String articuloDenominacion;
    private String fabricanteDenominacion;
    private String empaqueDenominacion;
    private String medidaBase;
    private BigDecimal volumenLibre;
    private BigDecimal costeMedioPonderado;
    private LocalDateTime actualizadoEn;

    public AlmacenNivelResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFichaId() { return fichaId; }
    public void setFichaId(Long fichaId) { this.fichaId = fichaId; }
    public String getCodigoEan() { return codigoEan; }
    public void setCodigoEan(String codigoEan) { this.codigoEan = codigoEan; }
    public String getArticuloDenominacion() { return articuloDenominacion; }
    public void setArticuloDenominacion(String articuloDenominacion) { this.articuloDenominacion = articuloDenominacion; }
    public String getFabricanteDenominacion() { return fabricanteDenominacion; }
    public void setFabricanteDenominacion(String fabricanteDenominacion) { this.fabricanteDenominacion = fabricanteDenominacion; }
    public String getEmpaqueDenominacion() { return empaqueDenominacion; }
    public void setEmpaqueDenominacion(String empaqueDenominacion) { this.empaqueDenominacion = empaqueDenominacion; }
    public String getMedidaBase() { return medidaBase; }
    public void setMedidaBase(String medidaBase) { this.medidaBase = medidaBase; }
    public BigDecimal getVolumenLibre() { return volumenLibre; }
    public void setVolumenLibre(BigDecimal volumenLibre) { this.volumenLibre = volumenLibre; }
    public BigDecimal getCosteMedioPonderado() { return costeMedioPonderado; }
    public void setCosteMedioPonderado(BigDecimal costeMedioPonderado) { this.costeMedioPonderado = costeMedioPonderado; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
