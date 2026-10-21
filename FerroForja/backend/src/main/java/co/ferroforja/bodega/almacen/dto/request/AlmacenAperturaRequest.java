package co.ferroforja.bodega.almacen.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class AlmacenAperturaRequest {

    @NotNull(message = "La referencia es obligatoria")
    private Long fichaId;

    @NotNull(message = "La cantidad inicial es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a cero")
    private BigDecimal volumenApertura;

    @NotNull(message = "El costo unitario de referencia es obligatorio")
    @DecimalMin(value = "0.01", message = "El costo unitario debe ser mayor a cero")
    private BigDecimal costeIndividual;

    public AlmacenAperturaRequest() {}

    public Long getFichaId() { return fichaId; }
    public void setFichaId(Long fichaId) { this.fichaId = fichaId; }

    public BigDecimal getVolumenApertura() { return volumenApertura; }
    public void setVolumenApertura(BigDecimal volumenApertura) { this.volumenApertura = volumenApertura; }

    public BigDecimal getCosteIndividual() { return costeIndividual; }
    public void setCosteIndividual(BigDecimal costeIndividual) { this.costeIndividual = costeIndividual; }
}
