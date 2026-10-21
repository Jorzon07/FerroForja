package co.ferroforja.bodega.catalogo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "articulo")
public class Articulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "denominacion", nullable = false, length = 100)
    private String denominacion;

    @Column(name = "resena", length = 255)
    private String resena;

    @Column(name = "habilitado", nullable = false)
    private boolean habilitado = true;

    public Articulo() {}

    public Articulo(Long id, String denominacion, String resena, boolean habilitado) {
        this.id = id;
        this.denominacion = denominacion;
        this.resena = resena;
        this.habilitado = habilitado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDenominacion() { return denominacion; }
    public void setDenominacion(String denominacion) { this.denominacion = denominacion; }

    public String getResena() { return resena; }
    public void setResena(String resena) { this.resena = resena; }

    public boolean isHabilitado() { return habilitado; }
    public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }
}
