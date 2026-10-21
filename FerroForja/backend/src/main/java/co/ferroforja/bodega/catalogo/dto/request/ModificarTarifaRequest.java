package co.ferroforja.bodega.catalogo.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ModificarTarifaRequest {

    @NotNull(message = "El nuevo precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    private BigDecimal nuevoTarifa;

    private String causa;

    public ModificarTarifaRequest() {}

    public ModificarTarifaRequest(BigDecimal nuevoTarifa, String causa) {
        this.nuevoTarifa = nuevoTarifa;
        this.causa = causa;
    }

    public BigDecimal getNuevoTarifa() { return nuevoTarifa; }
    public void setNuevoTarifa(BigDecimal nuevoTarifa) { this.nuevoTarifa = nuevoTarifa; }

    public String getCausa() { return causa; }
    public void setCausa(String causa) { this.causa = causa; }
}
