package co.ferroforja.bodega.operarios.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "perfil")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "denominacion", nullable = false, unique = true, length = 40)
    private String denominacion;

    @Column(name = "resena", length = 150)
    private String resena;

    public Perfil() {}

    public Perfil(Long id, String denominacion, String resena) {
        this.id = id;
        this.denominacion = denominacion;
        this.resena = resena;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDenominacion() { return denominacion; }
    public void setDenominacion(String denominacion) { this.denominacion = denominacion; }

    public String getResena() { return resena; }
    public void setResena(String resena) { this.resena = resena; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Perfil perfil = (Perfil) o;
        return Objects.equals(id, perfil.id) || Objects.equals(denominacion, perfil.denominacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(denominacion);
    }
}
