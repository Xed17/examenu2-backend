package pe.upeu.ecorecicla.dominio;

import jakarta.persistence.*;

@Entity
@Table(name = "privilegio")
public class Privilegio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String codigo;

    public Privilegio() {}

    public Privilegio(Long id, String codigo) {
        this.id = id;
        this.codigo = codigo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
}

