package co.ferroforja.bodega.operarios.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

public class OperarioResponse {
    private Long id;
    private String username;
    private String nombreVisible;
    private String email;
    private boolean habilitado;
    private Set<String> perfiles;
    private LocalDateTime creadoEn;

    public OperarioResponse() {}

    public OperarioResponse(Long id, String username, String nombreVisible, String email, boolean habilitado, Set<String> perfiles, LocalDateTime creadoEn) {
        this.id = id;
        this.username = username;
        this.nombreVisible = nombreVisible;
        this.email = email;
        this.habilitado = habilitado;
        this.perfiles = perfiles;
        this.creadoEn = creadoEn;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNombreVisible() { return nombreVisible; }
    public void setNombreVisible(String nombreVisible) { this.nombreVisible = nombreVisible; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isHabilitado() { return habilitado; }
    public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }
    public Set<String> getPerfiles() { return perfiles; }
    public void setPerfiles(Set<String> perfiles) { this.perfiles = perfiles; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
