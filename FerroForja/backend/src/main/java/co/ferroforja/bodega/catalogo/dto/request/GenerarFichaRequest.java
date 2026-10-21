package co.ferroforja.bodega.catalogo.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class GenerarFichaRequest {

    private Long articuloId;
    private String articuloDenominacion;

    private Long fabricanteId;
    private String fabricanteDenominacion;

    private Long empaqueId;
    private String empaqueDenominacion;

    private Long medidaBaseId;
    private String medidaBaseClave;

    @NotBlank(message = "El código de barras es obligatorio")
    @Size(min = 3, max = 50, message = "El código de barras debe tener entre 3 y 50 caracteres")
    private String codigoEan;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio de venta debe ser mayor a cero")
    private BigDecimal tarifaPublico;

    public GenerarFichaRequest() {}

    public Long getArticuloId() { return articuloId; }
    public void setArticuloId(Long articuloId) { this.articuloId = articuloId; }

    public String getArticuloDenominacion() { return articuloDenominacion; }
    public void setArticuloDenominacion(String articuloDenominacion) { this.articuloDenominacion = articuloDenominacion; }

    public Long getFabricanteId() { return fabricanteId; }
    public void setFabricanteId(Long fabricanteId) { this.fabricanteId = fabricanteId; }

    public String getFabricanteDenominacion() { return fabricanteDenominacion; }
    public void setFabricanteDenominacion(String fabricanteDenominacion) { this.fabricanteDenominacion = fabricanteDenominacion; }

    public Long getEmpaqueId() { return empaqueId; }
    public void setEmpaqueId(Long empaqueId) { this.empaqueId = empaqueId; }

    public String getEmpaqueDenominacion() { return empaqueDenominacion; }
    public void setEmpaqueDenominacion(String empaqueDenominacion) { this.empaqueDenominacion = empaqueDenominacion; }

    public Long getMedidaBaseId() { return medidaBaseId; }
    public void setMedidaBaseId(Long medidaBaseId) { this.medidaBaseId = medidaBaseId; }

    public String getMedidaBaseClave() { return medidaBaseClave; }
    public void setMedidaBaseClave(String medidaBaseClave) { this.medidaBaseClave = medidaBaseClave; }

    public String getCodigoEan() { return codigoEan; }
    public void setCodigoEan(String codigoEan) { this.codigoEan = codigoEan; }

    public BigDecimal getTarifaPublico() { return tarifaPublico; }
    public void setTarifaPublico(BigDecimal tarifaPublico) { this.tarifaPublico = tarifaPublico; }
}
