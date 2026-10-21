package co.ferroforja.bodega.socios.dto.response;

public class SocioResponse {
    private Long id;
    private String tipoIdentificacion;
    private String nroIdentificacion;
    private String nombreLegal;
    private String tipoPersona;
    private String celular;
    private String email;
    private String ubicacion;
    private boolean esComprador;
    private boolean esSuministrador;
    private boolean habilitado;

    public SocioResponse() {}

    public SocioResponse(Long id, String tipoIdentificacion, String nroIdentificacion, String nombreLegal,
                           String tipoPersona, String celular, String email, String ubicacion,
                           boolean esComprador, boolean esSuministrador, boolean habilitado) {
        this.id = id;
        this.tipoIdentificacion = tipoIdentificacion;
        this.nroIdentificacion = nroIdentificacion;
        this.nombreLegal = nombreLegal;
        this.tipoPersona = tipoPersona;
        this.celular = celular;
        this.email = email;
        this.ubicacion = ubicacion;
        this.esComprador = esComprador;
        this.esSuministrador = esSuministrador;
        this.habilitado = habilitado;
    }

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
