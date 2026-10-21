package co.ferroforja.bodega.socios.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class GenerarSocioRequest {

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Pattern(regexp = "^(CC|NIT|CE|PASAPORTE)$", message = "Tipo de documento inválido (CC, NIT, CE, PASAPORTE)")
    private String tipoIdentificacion;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(min = 4, max = 30, message = "El número de documento debe tener entre 4 y 30 caracteres")
    @Pattern(regexp = "^[0-9]+(-[0-9kK])?$", message = "El número de documento debe contener únicamente dígitos numéricos (o guión para dígito de verificación en NIT)")
    private String nroIdentificacion;

    @NotBlank(message = "La razón social o nombre completo es obligatoria")
    @Size(min = 2, max = 150, message = "La razón social debe tener entre 2 y 150 caracteres")
    private String nombreLegal;

    @NotBlank(message = "El tipo de persona es obligatorio")
    @Pattern(regexp = "^(NATURAL|EMPRESA)$", message = "Tipo de persona inválido (NATURAL o JURIDICA)")
    private String tipoPersona;

    @Pattern(regexp = "^$|^[0-9+\\s-]{7,25}$", message = "El teléfono debe contener únicamente dígitos numéricos (mínimo 7)")
    private String celular;
    private String email;
    private String ubicacion;

    @NotNull(message = "Debe indicar si es cliente")
    private Boolean esComprador;

    @NotNull(message = "Debe indicar si es proveedor")
    private Boolean esSuministrador;

    public GenerarSocioRequest() {}

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

    public Boolean getEsComprador() { return esComprador; }
    public void setEsComprador(Boolean esComprador) { this.esComprador = esComprador; }

    public Boolean getEsSuministrador() { return esSuministrador; }
    public void setEsSuministrador(Boolean esSuministrador) { this.esSuministrador = esSuministrador; }
}
