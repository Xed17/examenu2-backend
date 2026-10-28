package pe.upeu.ecorecicla.dominio;

import jakarta.persistence.*;

@Entity
@Table(name = "punto_acopio")
public class PuntoAcopio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(nullable = false, length = 250)
    private String direccion;

    @Column(nullable = false, length = 50)
    private String zona;

    @Column(name = "material_habitual", nullable = false, length = 50)
    private String materialHabitual;

    @Column(nullable = false)
    private Boolean activo = true;

    public PuntoAcopio() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }

    public String getMaterialHabitual() { return materialHabitual; }
    public void setMaterialHabitual(String materialHabitual) { this.materialHabitual = materialHabitual; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}

