package co.ferroforja.bodega.restituciones.dto.response;

import java.math.BigDecimal;

public class RestitucionDetalleResponse {
    private Long id;
    private Long fichaId;
    private String articuloDenominacion;
    private String codigoEan;
    private BigDecimal volumen;
    private BigDecimal costeIndividualAplicado;

    public RestitucionDetalleResponse() {}

    public RestitucionDetalleResponse(Long id, Long fichaId, String articuloDenominacion, String codigoEan,
                                   BigDecimal volumen, BigDecimal costeIndividualAplicado) {
        this.id = id;
        this.fichaId = fichaId;
        this.articuloDenominacion = articuloDenominacion;
        this.codigoEan = codigoEan;
        this.volumen = volumen;
        this.costeIndividualAplicado = costeIndividualAplicado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFichaId() { return fichaId; }
    public void setFichaId(Long fichaId) { this.fichaId = fichaId; }
    public String getArticuloDenominacion() { return articuloDenominacion; }
    public void setArticuloDenominacion(String articuloDenominacion) { this.articuloDenominacion = articuloDenominacion; }
    public String getCodigoEan() { return codigoEan; }
    public void setCodigoEan(String codigoEan) { this.codigoEan = codigoEan; }
    public BigDecimal getVolumen() { return volumen; }
    public void setVolumen(BigDecimal volumen) { this.volumen = volumen; }
    public BigDecimal getCosteIndividualAplicado() { return costeIndividualAplicado; }
    public void setCosteIndividualAplicado(BigDecimal costeIndividualAplicado) { this.costeIndividualAplicado = costeIndividualAplicado; }
}
