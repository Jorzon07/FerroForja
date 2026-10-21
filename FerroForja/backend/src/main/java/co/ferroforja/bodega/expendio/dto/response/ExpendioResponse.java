package co.ferroforja.bodega.expendio.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ExpendioResponse {
    private Long id;
    private String correlativo;
    private Long compradorId;
    private String compradorDenominacion;
    private String compradorFolio;
    private LocalDateTime marcaTiempo;
    private BigDecimal subtotal;
    private BigDecimal total;
    private BigDecimal importeAbonadoEfectivo;
    private BigDecimal vueltoEntregado;
    private String situacion;
    private String causaAnulacion;
    private String operarioDenominacion;
    private List<ExpendioDetalleResponse> detalles;

    public ExpendioResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCorrelativo() { return correlativo; }
    public void setCorrelativo(String correlativo) { this.correlativo = correlativo; }
    public Long getCompradorId() { return compradorId; }
    public void setCompradorId(Long compradorId) { this.compradorId = compradorId; }
    public String getCompradorDenominacion() { return compradorDenominacion; }
    public void setCompradorDenominacion(String compradorDenominacion) { this.compradorDenominacion = compradorDenominacion; }
    public String getCompradorFolio() { return compradorFolio; }
    public void setCompradorFolio(String compradorFolio) { this.compradorFolio = compradorFolio; }
    public LocalDateTime getMarcaTiempo() { return marcaTiempo; }
    public void setMarcaTiempo(LocalDateTime marcaTiempo) { this.marcaTiempo = marcaTiempo; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public BigDecimal getImporteAbonadoEfectivo() { return importeAbonadoEfectivo; }
    public void setImporteAbonadoEfectivo(BigDecimal importeAbonadoEfectivo) { this.importeAbonadoEfectivo = importeAbonadoEfectivo; }
    public BigDecimal getVueltoEntregado() { return vueltoEntregado; }
    public void setVueltoEntregado(BigDecimal vueltoEntregado) { this.vueltoEntregado = vueltoEntregado; }
    public String getSituacion() { return situacion; }
    public void setSituacion(String situacion) { this.situacion = situacion; }
    public String getCausaAnulacion() { return causaAnulacion; }
    public void setCausaAnulacion(String causaAnulacion) { this.causaAnulacion = causaAnulacion; }
    public String getOperarioDenominacion() { return operarioDenominacion; }
    public void setOperarioDenominacion(String operarioDenominacion) { this.operarioDenominacion = operarioDenominacion; }
    public List<ExpendioDetalleResponse> getDetalles() { return detalles; }
    public void setDetalles(List<ExpendioDetalleResponse> detalles) { this.detalles = detalles; }
}
