package co.ferroforja.bodega.folios.entity;

import jakarta.persistence.*;

/**
 * Entidad TiqueteCorrelativo: administra la numeración secuencial
 * independiente y correlativa de comprobantes (RN-12).
 */
@Entity
@Table(name = "tiquete_correlativo")
public class TiqueteCorrelativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_identificacion", nullable = false, unique = true, length = 25)
    private String tipoIdentificacion; // VENTA, COMPRA, DEVOLUCION, AJUSTE

    @Column(name = "prefijo", nullable = false, length = 10)
    private String prefijo;

    @Column(name = "ultimo_nro", nullable = false)
    private Long ultimoNro = 0L;

    public TiqueteCorrelativo() {}

    public TiqueteCorrelativo(String tipoIdentificacion, String prefijo, Long ultimoNro) {
        this.tipoIdentificacion = tipoIdentificacion;
        this.prefijo = prefijo;
        this.ultimoNro = ultimoNro;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoIdentificacion() { return tipoIdentificacion; }
    public void setTipoIdentificacion(String tipoIdentificacion) { this.tipoIdentificacion = tipoIdentificacion; }

    public String getPrefijo() { return prefijo; }
    public void setPrefijo(String prefijo) { this.prefijo = prefijo; }

    public Long getUltimoNro() { return ultimoNro; }
    public void setUltimoNro(Long ultimoNro) { this.ultimoNro = ultimoNro; }

    public String generarSiguienteCorrelativo() {
        this.ultimoNro++;
        return String.format("%s%06d", prefijo, ultimoNro);
    }
}
