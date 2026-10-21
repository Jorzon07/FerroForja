package co.ferroforja.bodega.operarios.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

public class GenerarOperarioRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 120, message = "El nombre completo debe tener entre 3 y 120 caracteres")
    private String nombreVisible;

    private String email;

    @NotEmpty(message = "Debe asignar al menos un rol al usuario")
    private Set<String> perfiles; // ROLE_ADMINISTRADOR, ROLE_USUARIO_OPERATIVO

    public GenerarOperarioRequest() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNombreVisible() { return nombreVisible; }
    public void setNombreVisible(String nombreVisible) { this.nombreVisible = nombreVisible; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Set<String> getPerfiles() { return perfiles; }
    public void setPerfiles(Set<String> perfiles) { this.perfiles = perfiles; }
}
