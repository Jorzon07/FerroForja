package co.ferroforja.bodega.folios.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para la visualización e impresión de comprobantes comerciales
 * internos (Ventas, Compras, Devoluciones, Ajustes) simulando formato de mostrador.
 */
public class FolioTiqueteResponse {
    private String tipoIdentificacion; // VENTA, COMPRA, DEVOLUCION, AJUSTE
    private String correlativo;
    private LocalDateTime marcaTiempo;
    private String emisorDenominacion = "FerroForja";
    private String emisorNit = "900.876.543-2";
    private String emisorUbicacion = "Calle 10 # 45-20, Bogotá D.C.";
    private String emisorCelular = "+57 (601) 234-5678";
    
    private String socioDenominacion;
    private String socioFolio;
    private String socioUbicacion;
    private String socioCelular;
    
    private BigDecimal subtotal;
    private BigDecimal total;
    private BigDecimal importeAbonado;
    private BigDecimal vuelto;
    private String situacion;
    private String operarioResponsable;
    private String causa; // En caso de ajuste o cancelación
    
    private List<DetalleTiquete> detalles;

    public static class DetalleTiquete {
        private String codigoEan;
        private String resena;
        private BigDecimal volumen;
        private String medida;
        private BigDecimal valorIndividual;
        private BigDecimal subtotal;

        public DetalleTiquete() {}

        public DetalleTiquete(String codigoEan, String resena, BigDecimal volumen, String medida, BigDecimal valorIndividual, BigDecimal subtotal) {
            this.codigoEan = codigoEan;
            this.resena = resena;
            this.volumen = volumen;
            this.medida = medida;
            this.valorIndividual = valorIndividual;
            this.subtotal = subtotal;
        }

        public String getCodigoEan() { return codigoEan; }
        public void setCodigoEan(String codigoEan) { this.codigoEan = codigoEan; }
        public String getResena() { return resena; }
        public void setResena(String resena) { this.resena = resena; }
        public BigDecimal getVolumen() { return volumen; }
        public void setVolumen(BigDecimal volumen) { this.volumen = volumen; }
        public String getMedida() { return medida; }
        public void setMedida(String medida) { this.medida = medida; }
        public BigDecimal getValorIndividual() { return valorIndividual; }
        public void setValorIndividual(BigDecimal valorIndividual) { this.valorIndividual = valorIndividual; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    }

    public FolioTiqueteResponse() {}

    public String getTipoIdentificacion() { return tipoIdentificacion; }
    public void setTipoIdentificacion(String tipoIdentificacion) { this.tipoIdentificacion = tipoIdentificacion; }
    public String getCorrelativo() { return correlativo; }
    public void setCorrelativo(String correlativo) { this.correlativo = correlativo; }
    public LocalDateTime getMarcaTiempo() { return marcaTiempo; }
    public void setMarcaTiempo(LocalDateTime marcaTiempo) { this.marcaTiempo = marcaTiempo; }
    public String getEmisorDenominacion() { return emisorDenominacion; }
    public void setEmisorDenominacion(String emisorDenominacion) { this.emisorDenominacion = emisorDenominacion; }
    public String getEmisorNit() { return emisorNit; }
    public void setEmisorNit(String emisorNit) { this.emisorNit = emisorNit; }
    public String getEmisorUbicacion() { return emisorUbicacion; }
    public void setEmisorUbicacion(String emisorUbicacion) { this.emisorUbicacion = emisorUbicacion; }
    public String getEmisorCelular() { return emisorCelular; }
    public void setEmisorCelular(String emisorCelular) { this.emisorCelular = emisorCelular; }
    public String getSocioDenominacion() { return socioDenominacion; }
    public void setSocioDenominacion(String socioDenominacion) { this.socioDenominacion = socioDenominacion; }
    public String getSocioFolio() { return socioFolio; }
    public void setSocioFolio(String socioFolio) { this.socioFolio = socioFolio; }
    public String getSocioUbicacion() { return socioUbicacion; }
    public void setSocioUbicacion(String socioUbicacion) { this.socioUbicacion = socioUbicacion; }
    public String getSocioCelular() { return socioCelular; }
    public void setSocioCelular(String socioCelular) { this.socioCelular = socioCelular; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public BigDecimal getImporteAbonado() { return importeAbonado; }
    public void setImporteAbonado(BigDecimal importeAbonado) { this.importeAbonado = importeAbonado; }
    public BigDecimal getVuelto() { return vuelto; }
    public void setVuelto(BigDecimal vuelto) { this.vuelto = vuelto; }
    public String getSituacion() { return situacion; }
    public void setSituacion(String situacion) { this.situacion = situacion; }
    public String getOperarioResponsable() { return operarioResponsable; }
    public void setOperarioResponsable(String operarioResponsable) { this.operarioResponsable = operarioResponsable; }
    public String getCausa() { return causa; }
    public void setCausa(String causa) { this.causa = causa; }
    public List<DetalleTiquete> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleTiquete> detalles) { this.detalles = detalles; }
}
