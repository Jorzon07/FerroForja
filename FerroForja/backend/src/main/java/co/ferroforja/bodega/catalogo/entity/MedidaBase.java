package co.ferroforja.bodega.catalogo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "medida_base")
public class MedidaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clave", nullable = false, unique = true, length = 10)
    private String clave;

    @Column(name = "denominacion", nullable = false, length = 50)
    private String denominacion;

    public MedidaBase() {}

    public MedidaBase(Long id, String clave, String denominacion) {
        this.id = id;
        this.clave = clave;
        this.denominacion = denominacion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public String getDenominacion() { return denominacion; }
    public void setDenominacion(String denominacion) { this.denominacion = denominacion; }
}
