package co.ferroforja.bodega.expendio.dto.response;

import java.math.BigDecimal;

public class ExpendioDetalleResponse {
    private Long id;
    private Long fichaId;
    private String articuloDenominacion;
    private String codigoEan;
    private BigDecimal volumen;
    private BigDecimal tarifaIndividual;
    private BigDecimal costeCongeladoIndividual;
    private BigDecimal subtotal;

    public ExpendioDetalleResponse() {}

    public ExpendioDetalleResponse(Long id, Long fichaId, String articuloDenominacion, String codigoEan,
                              BigDecimal volumen, BigDecimal tarifaIndividual, BigDecimal costeCongeladoIndividual,
                              BigDecimal subtotal) {
        this.id = id;
        this.fichaId = fichaId;
        this.articuloDenominacion = articuloDenominacion;
        this.codigoEan = codigoEan;
        this.volumen = volumen;
        this.tarifaIndividual = tarifaIndividual;
        this.costeCongeladoIndividual = costeCongeladoIndividual;
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
    public BigDecimal getTarifaIndividual() { return tarifaIndividual; }
    public void setTarifaIndividual(BigDecimal tarifaIndividual) { this.tarifaIndividual = tarifaIndividual; }
    public BigDecimal getCosteCongeladoIndividual() { return costeCongeladoIndividual; }
    public void setCosteCongeladoIndividual(BigDecimal costeCongeladoIndividual) { this.costeCongeladoIndividual = costeCongeladoIndividual; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
