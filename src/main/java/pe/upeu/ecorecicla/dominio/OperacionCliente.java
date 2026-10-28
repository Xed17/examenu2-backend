package pe.upeu.ecorecicla.dominio;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "operacion_cliente",
    uniqueConstraints = @UniqueConstraint(name = "uq_operacion_usuario_uuid", columnNames = {"usuario_id", "operacion_uuid"})
)
public class OperacionCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "operacion_uuid", nullable = false, length = 100)
    private String operacionUuid;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(name = "recurso_id", nullable = false)
    private Long recursoId;

    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn = Instant.now();

    public OperacionCliente() {}

    public OperacionCliente(Long usuarioId, String operacionUuid, String tipo, Long recursoId) {
        this.usuarioId = usuarioId;
        this.operacionUuid = operacionUuid;
        this.tipo = tipo;
        this.recursoId = recursoId;
        this.creadaEn = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getOperacionUuid() { return operacionUuid; }
    public void setOperacionUuid(String operacionUuid) { this.operacionUuid = operacionUuid; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Long getRecursoId() { return recursoId; }
    public void setRecursoId(Long recursoId) { this.recursoId = recursoId; }

    public Instant getCreadaEn() { return creadaEn; }
    public void setCreadaEn(Instant creadaEn) { this.creadaEn = creadaEn; }
}

