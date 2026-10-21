package co.ferroforja.bodega.acceso.dto.response;

import java.util.List;

public class AccesoResponse {
    private Long id;
    private String username;
    private String nombreVisible;
    private String email;
    private List<String> perfiles;

    public AccesoResponse() {}

    public AccesoResponse(Long id, String username, String nombreVisible, String email, List<String> perfiles) {
        this.id = id;
        this.username = username;
        this.nombreVisible = nombreVisible;
        this.email = email;
        this.perfiles = perfiles;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNombreVisible() { return nombreVisible; }
    public void setNombreVisible(String nombreVisible) { this.nombreVisible = nombreVisible; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<String> getPerfiles() { return perfiles; }
    public void setPerfiles(List<String> perfiles) { this.perfiles = perfiles; }
}
