package co.ferroforja.bodega.catalogo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "fabricante")
public class Fabricante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "denominacion", nullable = false, unique = true, length = 80)
    private String denominacion;

    @Column(name = "habilitado", nullable = false)
    private boolean habilitado = true;

    public Fabricante() {}

    public Fabricante(Long id, String denominacion, boolean habilitado) {
        this.id = id;
        this.denominacion = denominacion;
        this.habilitado = habilitado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDenominacion() { return denominacion; }
    public void setDenominacion(String denominacion) { this.denominacion = denominacion; }

    public boolean isHabilitado() { return habilitado; }
    public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }
}
