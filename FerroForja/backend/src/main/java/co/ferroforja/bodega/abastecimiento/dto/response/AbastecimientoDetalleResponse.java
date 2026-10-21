package co.ferroforja.bodega.abastecimiento.dto.response;

import java.math.BigDecimal;

public class AbastecimientoDetalleResponse {
    private Long id;
    private Long fichaId;
    private String articuloDenominacion;
    private String codigoEan;
    private BigDecimal volumen;
    private BigDecimal costeIndividual;
    private BigDecimal subtotal;

    public AbastecimientoDetalleResponse() {}

    public AbastecimientoDetalleResponse(Long id, Long fichaId, String articuloDenominacion, String codigoEan,
                               BigDecimal volumen, BigDecimal costeIndividual, BigDecimal subtotal) {
        this.id = id;
        this.fichaId = fichaId;
        this.articuloDenominacion = articuloDenominacion;
        this.codigoEan = codigoEan;
        this.volumen = volumen;
        this.costeIndividual = costeIndividual;
        this.subtotal = subtotal;
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
    public BigDecimal getCosteIndividual() { return costeIndividual; }
    public void setCosteIndividual(BigDecimal costeIndividual) { this.costeIndividual = costeIndividual; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
