package pe.upeu.ecorecicla.dominio;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "rol_privilegio",
        joinColumns = @JoinColumn(name = "rol_id"),
        inverseJoinColumns = @JoinColumn(name = "privilegio_id")
    )
    private Set<Privilegio> privilegios = new HashSet<>();

    public Rol() {}

    public Rol(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Set<Privilegio> getPrivilegios() { return privilegios; }
    public void setPrivilegios(Set<Privilegio> privilegios) { this.privilegios = privilegios; }
}

