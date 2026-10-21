package co.ferroforja.bodega.abastecimiento.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AnularFolioRequest {

    @NotBlank(message = "El motivo de cancelación es obligatorio")
    @Size(min = 5, max = 255, message = "El motivo debe tener entre 5 y 255 caracteres")
    private String causa;

    public AnularFolioRequest() {}

    public AnularFolioRequest(String causa) {
        this.causa = causa;
    }

    public String getCausa() { return causa; }
    public void setCausa(String causa) { this.causa = causa; }
}
