package co.ferroforja.bodega.expendio.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ExpendioDetalleRequest {

    @NotNull(message = "La referencia de producto es obligatoria")
    private Long fichaId;

    @NotNull(message = "La cantidad vendida es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a cero")
    private BigDecimal volumen;

    public ExpendioDetalleRequest() {}

    public ExpendioDetalleRequest(Long fichaId, BigDecimal volumen) {
        this.fichaId = fichaId;
        this.volumen = volumen;
    }

    public Long getFichaId() { return fichaId; }
    public void setFichaId(Long fichaId) { this.fichaId = fichaId; }

    public BigDecimal getVolumen() { return volumen; }
    public void setVolumen(BigDecimal volumen) { this.volumen = volumen; }
}
