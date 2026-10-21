package co.ferroforja.bodega.socios.entity;

import jakarta.persistence.*;

/**
 * Entidad Tercero: representa clientes y proveedores de forma unificada (RN-10).
 */
@Entity
@Table(name = "socio")
public class Socio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_identificacion", nullable = false, length = 10)
    private String tipoIdentificacion; // CC, NIT, CE, PASAPORTE

    @Column(name = "nro_identificacion", nullable = false, unique = true, length = 30)
    private String nroIdentificacion;

    @Column(name = "nombre_legal", nullable = false, length = 150)
    private String nombreLegal;

    @Column(name = "tipo_persona", nullable = false, length = 15)
    private String tipoPersona; // NATURAL, JURIDICA

    @Column(name = "celular", length = 30)
    private String celular;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "ubicacion", length = 150)
    private String ubicacion;

    @Column(name = "es_comprador", nullable = false)
    private boolean esComprador = true;

    @Column(name = "es_suministrador", nullable = false)
    private boolean esSuministrador = false;

    @Column(name = "habilitado", nullable = false)
    private boolean habilitado = true;

    public Socio() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoIdentificacion() { return tipoIdentificacion; }
    public void setTipoIdentificacion(String tipoIdentificacion) { this.tipoIdentificacion = tipoIdentificacion; }

    public String getNroIdentificacion() { return nroIdentificacion; }
    public void setNroIdentificacion(String nroIdentificacion) { this.nroIdentificacion = nroIdentificacion; }

    public String getNombreLegal() { return nombreLegal; }
    public void setNombreLegal(String nombreLegal) { this.nombreLegal = nombreLegal; }

    public String getTipoPersona() { return tipoPersona; }
    public void setTipoPersona(String tipoPersona) { this.tipoPersona = tipoPersona; }

    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public boolean isEsComprador() { return esComprador; }
    public void setEsComprador(boolean esComprador) { this.esComprador = esComprador; }

    public boolean isEsSuministrador() { return esSuministrador; }
    public void setEsSuministrador(boolean esSuministrador) { this.esSuministrador = esSuministrador; }

    public boolean isHabilitado() { return habilitado; }
    public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }
}
