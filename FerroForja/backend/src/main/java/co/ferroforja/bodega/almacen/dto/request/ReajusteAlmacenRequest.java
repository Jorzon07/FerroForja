package co.ferroforja.bodega.almacen.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class ReajusteAlmacenRequest {

    @NotNull(message = "La referencia es obligatoria")
    private Long fichaId;

    @NotBlank(message = "El tipo de ajuste es obligatorio")
    @Pattern(regexp = "^(INCREMENTO|DISMINUCION)$", message = "Tipo de ajuste inválido (INCREMENTO o DISMINUCION)")
    private String tipoReajuste;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a cero")
    private BigDecimal volumen;

    @NotBlank(message = "El motivo del ajuste es obligatorio")
    @Size(min = 5, max = 255, message = "El motivo debe tener entre 5 y 255 caracteres")
    private String causa;

    public ReajusteAlmacenRequest() {}

    public Long getFichaId() { return fichaId; }
    public void setFichaId(Long fichaId) { this.fichaId = fichaId; }

    public String getTipoReajuste() { return tipoReajuste; }
    public void setTipoReajuste(String tipoReajuste) { this.tipoReajuste = tipoReajuste; }

    public BigDecimal getVolumen() { return volumen; }
    public void setVolumen(BigDecimal volumen) { this.volumen = volumen; }

    public String getCausa() { return causa; }
    public void setCausa(String causa) { this.causa = causa; }
}
