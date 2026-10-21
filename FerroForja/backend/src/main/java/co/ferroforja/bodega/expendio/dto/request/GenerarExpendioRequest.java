package co.ferroforja.bodega.expendio.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para registrar una venta en mostrador.
 * Nota de seguridad: El cliente NO envía precios, costos ni totales;
 * el backend los calcula y certifica con el catálogo y CPP vigentes.
 */
public class GenerarExpendioRequest {

    @NotNull(message = "El cliente es obligatorio para registrar la venta (RN-05)")
    private Long compradorId;

    @NotNull(message = "El monto pagado en efectivo es obligatorio (RN-06)")
    @DecimalMin(value = "0.01", message = "El monto pagado debe ser mayor a cero")
    private BigDecimal importeAbonadoEfectivo;

    @NotEmpty(message = "La venta debe incluir al menos una línea de producto")
    @Valid
    private List<ExpendioDetalleRequest> detalles;

    public GenerarExpendioRequest() {}

    public Long getCompradorId() { return compradorId; }
    public void setCompradorId(Long compradorId) { this.compradorId = compradorId; }

    public BigDecimal getImporteAbonadoEfectivo() { return importeAbonadoEfectivo; }
    public void setImporteAbonadoEfectivo(BigDecimal importeAbonadoEfectivo) { this.importeAbonadoEfectivo = importeAbonadoEfectivo; }

    public List<ExpendioDetalleRequest> getDetalles() { return detalles; }
    public void setDetalles(List<ExpendioDetalleRequest> detalles) { this.detalles = detalles; }
}
