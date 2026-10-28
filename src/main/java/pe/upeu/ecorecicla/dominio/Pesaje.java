package pe.upeu.ecorecicla.dominio;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "pesaje")
public class Pesaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_punto_id", nullable = false)
    private RutaPunto rutaPunto;

    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    @Column(nullable = false, length = 50)
    private String material;

    @Column(name = "peso_bruto", nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoBruto;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tara;

    @Column(name = "peso_neto", nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoNeto;

    @Column(length = 250)
    private String observacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operario_id", nullable = false)
    private Usuario operario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPesaje estado = EstadoPesaje.REGISTRADO;

    @Column(name = "motivo_anulacion", length = 250)
    private String motivoAnulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anulado_por")
    private Usuario anuladoPor;

    @Column(name = "anulado_en")
    private Instant anuladoEn;

    @Column(name = "actualizado_en")
    private Instant actualizadoEn;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    public Pesaje() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RutaPunto getRutaPunto() { return rutaPunto; }
    public void setRutaPunto(RutaPunto rutaPunto) { this.rutaPunto = rutaPunto; }

    public Instant getFechaHora() { return fechaHora; }
    public void setFechaHora(Instant fechaHora) { this.fechaHora = fechaHora; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public BigDecimal getPesoBruto() { return pesoBruto; }
    public void setPesoBruto(BigDecimal pesoBruto) { this.pesoBruto = pesoBruto; }

    public BigDecimal getTara() { return tara; }
    public void setTara(BigDecimal tara) { this.tara = tara; }

    public BigDecimal getPesoNeto() { return pesoNeto; }
    public void setPesoNeto(BigDecimal pesoNeto) { this.pesoNeto = pesoNeto; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public Usuario getOperario() { return operario; }
    public void setOperario(Usuario operario) { this.operario = operario; }

    public EstadoPesaje getEstado() { return estado; }
    public void setEstado(EstadoPesaje estado) { this.estado = estado; }

    public String getMotivoAnulacion() { return motivoAnulacion; }
    public void setMotivoAnulacion(String motivoAnulacion) { this.motivoAnulacion = motivoAnulacion; }

    public Usuario getAnuladoPor() { return anuladoPor; }
    public void setAnuladoPor(Usuario anuladoPor) { this.anuladoPor = anuladoPor; }

    public Instant getAnuladoEn() { return anuladoEn; }
    public void setAnuladoEn(Instant anuladoEn) { this.anuladoEn = anuladoEn; }

    public Instant getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(Instant actualizadoEn) { this.actualizadoEn = actualizadoEn; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
