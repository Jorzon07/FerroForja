package co.ferroforja.bodega.almacen.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AlmacenAsientoResponse {
    private Long id;
    private Long fichaId;
    private String articuloDenominacion;
    private String tipoAsiento;
    private BigDecimal volumen;
    private BigDecimal costeIndividualAsiento;
    private BigDecimal nivelObtenido;
    private String folioTipo;
    private Long folioId;
    private String causa;
    private LocalDateTime marcaTiempo;
    private String operarioDenominacion;

    public AlmacenAsientoResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFichaId() { return fichaId; }
    public void setFichaId(Long fichaId) { this.fichaId = fichaId; }
    public String getArticuloDenominacion() { return articuloDenominacion; }
    public void setArticuloDenominacion(String articuloDenominacion) { this.articuloDenominacion = articuloDenominacion; }
    public String getTipoAsiento() { return tipoAsiento; }
    public void setTipoAsiento(String tipoAsiento) { this.tipoAsiento = tipoAsiento; }
    public BigDecimal getVolumen() { return volumen; }
    public void setVolumen(BigDecimal volumen) { this.volumen = volumen; }
    public BigDecimal getCosteIndividualAsiento() { return costeIndividualAsiento; }
    public void setCosteIndividualAsiento(BigDecimal costeIndividualAsiento) { this.costeIndividualAsiento = costeIndividualAsiento; }
    public BigDecimal getNivelObtenido() { return nivelObtenido; }
    public void setNivelObtenido(BigDecimal nivelObtenido) { this.nivelObtenido = nivelObtenido; }
    public String getFolioTipo() { return folioTipo; }
    public void setFolioTipo(String folioTipo) { this.folioTipo = folioTipo; }
    public Long getFolioId() { return folioId; }
    public void setFolioId(Long folioId) { this.folioId = folioId; }
    public String getCausa() { return causa; }
    public void setCausa(String causa) { this.causa = causa; }
    public LocalDateTime getMarcaTiempo() { return marcaTiempo; }
    public void setMarcaTiempo(LocalDateTime marcaTiempo) { this.marcaTiempo = marcaTiempo; }
    public String getOperarioDenominacion() { return operarioDenominacion; }
    public void setOperarioDenominacion(String operarioDenominacion) { this.operarioDenominacion = operarioDenominacion; }
}
