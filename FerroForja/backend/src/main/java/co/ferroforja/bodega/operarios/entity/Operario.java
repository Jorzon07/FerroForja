package co.ferroforja.bodega.operarios.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "operario")
public class Operario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "nombre_visible", nullable = false, length = 120)
    private String nombreVisible;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "habilitado", nullable = false)
    private boolean habilitado = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "operario_perfil",
        joinColumns = @JoinColumn(name = "operario_id"),
        inverseJoinColumns = @JoinColumn(name = "perfil_id")
    )
    private Set<Perfil> perfiles = new HashSet<>();

    public Operario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getNombreVisible() { return nombreVisible; }
    public void setNombreVisible(String nombreVisible) { this.nombreVisible = nombreVisible; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isHabilitado() { return habilitado; }
    public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }

    public Set<Perfil> getPerfiles() { return perfiles; }
    public void setPerfiles(Set<Perfil> perfiles) { this.perfiles = perfiles; }

    @PreUpdate
    public void preUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}
