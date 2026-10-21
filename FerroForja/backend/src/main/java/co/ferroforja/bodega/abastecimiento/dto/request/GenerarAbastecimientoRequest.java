package co.ferroforja.bodega.abastecimiento.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class GenerarAbastecimientoRequest {

    @NotNull(message = "El proveedor es obligatorio")
    private Long suministradorId;

    @NotEmpty(message = "La compra debe contener al menos una línea")
    @Valid
    private List<AbastecimientoDetalleRequest> detalles;

    public GenerarAbastecimientoRequest() {}

    public Long getSuministradorId() { return suministradorId; }
    public void setSuministradorId(Long suministradorId) { this.suministradorId = suministradorId; }

    public List<AbastecimientoDetalleRequest> getDetalles() { return detalles; }
    public void setDetalles(List<AbastecimientoDetalleRequest> detalles) { this.detalles = detalles; }
}
