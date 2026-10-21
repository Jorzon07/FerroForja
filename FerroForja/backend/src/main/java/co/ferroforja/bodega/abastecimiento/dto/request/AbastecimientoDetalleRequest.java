package co.ferroforja.bodega.abastecimiento.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class AbastecimientoDetalleRequest {

    @NotNull(message = "La referencia de producto es obligatoria")
    private Long fichaId;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a cero")
    private BigDecimal volumen;

    @NotNull(message = "El costo unitario es obligatorio")
    @DecimalMin(value = "0.01", message = "El costo unitario debe ser mayor a cero")
    private BigDecimal costeIndividual;

    public AbastecimientoDetalleRequest() {}

    public AbastecimientoDetalleRequest(Long fichaId, BigDecimal volumen, BigDecimal costeIndividual) {
        this.fichaId = fichaId;
        this.volumen = volumen;
        this.costeIndividual = costeIndividual;
    }

    public Long getFichaId() { return fichaId; }
    public void setFichaId(Long fichaId) { this.fichaId = fichaId; }

    public BigDecimal getVolumen() { return volumen; }
    public void setVolumen(BigDecimal volumen) { this.volumen = volumen; }

    public BigDecimal getCosteIndividual() { return costeIndividual; }
    public void setCosteIndividual(BigDecimal costeIndividual) { this.costeIndividual = costeIndividual; }
}
