package pe.upeu.ecorecicla.dominio;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ruta_punto")
public class RutaPunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "punto_id", nullable = false)
    private PuntoAcopio punto;

    @Column(nullable = false)
    private Integer secuencia;

    @Column(name = "motivo_omision", length = 250)
    private String motivoOmision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "omitido_por")
    private Usuario omitidoPor;

    @Column(name = "omitido_en")
    private Instant omitidoEn;

    @OneToMany(mappedBy = "rutaPunto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("fechaHora DESC, id DESC")
    private List<Pesaje> pesajes = new ArrayList<>();

    public RutaPunto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Ruta getRuta() { return ruta; }
    public void setRuta(Ruta ruta) { this.ruta = ruta; }

    public PuntoAcopio getPunto() { return punto; }
    public void setPunto(PuntoAcopio punto) { this.punto = punto; }

    public Integer getSecuencia() { return secuencia; }
    public void setSecuencia(Integer secuencia) { this.secuencia = secuencia; }

    public String getMotivoOmision() { return motivoOmision; }
    public void setMotivoOmision(String motivoOmision) { this.motivoOmision = motivoOmision; }

    public Usuario getOmitidoPor() { return omitidoPor; }
    public void setOmitidoPor(Usuario omitidoPor) { this.omitidoPor = omitidoPor; }

    public Instant getOmitidoEn() { return omitidoEn; }
    public void setOmitidoEn(Instant omitidoEn) { this.omitidoEn = omitidoEn; }

    public List<Pesaje> getPesajes() { return pesajes; }
    public void setPesajes(List<Pesaje> pesajes) { this.pesajes = pesajes; }

    public boolean isAtendido() {
        if (motivoOmision != null && !motivoOmision.isBlank()) {
            return true;
        }
        for (Pesaje p : pesajes) {
            if (p.getEstado() != EstadoPesaje.ANULADO) {
                return true;
            }
        }
        return false;
    }
}

