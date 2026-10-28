package pe.upeu.ecorecicla.dominio;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "ruta")
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoRuta estado = EstadoRuta.ABIERTA;

    @Column(name = "cerrada_en")
    private Instant cerradaEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cerrada_por")
    private Usuario cerradaPor;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "ruta_usuario",
        joinColumns = @JoinColumn(name = "ruta_id"),
        inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private Set<Usuario> usuarios = new HashSet<>();

    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("secuencia ASC")
    private List<RutaPunto> puntos = new ArrayList<>();

    public Ruta() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public EstadoRuta getEstado() { return estado; }
    public void setEstado(EstadoRuta estado) { this.estado = estado; }

    public Instant getCerradaEn() { return cerradaEn; }
    public void setCerradaEn(Instant cerradaEn) { this.cerradaEn = cerradaEn; }

    public Usuario getCerradaPor() { return cerradaPor; }
    public void setCerradaPor(Usuario cerradaPor) { this.cerradaPor = cerradaPor; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public Set<Usuario> getUsuarios() { return usuarios; }
    public void setUsuarios(Set<Usuario> usuarios) { this.usuarios = usuarios; }

    public List<RutaPunto> getPuntos() { return puntos; }
    public void setPuntos(List<RutaPunto> puntos) { this.puntos = puntos; }
}

