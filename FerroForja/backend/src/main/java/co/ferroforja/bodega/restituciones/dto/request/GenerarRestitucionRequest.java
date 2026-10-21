package co.ferroforja.bodega.restituciones.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public class GenerarRestitucionRequest {

    private Long expendioId; // Opcional (si es nulo, es no vinculada)
    private Long compradorId; // Opcional si expendioId está presente

    @NotBlank(message = "El motivo de la devolución es obligatorio")
    @Size(min = 5, max = 255, message = "El motivo debe tener entre 5 y 255 caracteres")
    private String causa;

    @NotEmpty(message = "Debe incluir al menos un producto a devolver")
    @Valid
    private List<RestitucionDetalleRequest> detalles;

    public GenerarRestitucionRequest() {}

    public Long getExpendioId() { return expendioId; }
    public void setExpendioId(Long expendioId) { this.expendioId = expendioId; }

    public Long getCompradorId() { return compradorId; }
    public void setCompradorId(Long compradorId) { this.compradorId = compradorId; }

    public String getCausa() { return causa; }
    public void setCausa(String causa) { this.causa = causa; }

    public List<RestitucionDetalleRequest> getDetalles() { return detalles; }
    public void setDetalles(List<RestitucionDetalleRequest> detalles) { this.detalles = detalles; }
}
