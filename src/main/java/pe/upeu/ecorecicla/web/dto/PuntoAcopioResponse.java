package pe.upeu.ecorecicla.web.dto;

public record PuntoAcopioResponse(
    Long id,
    String razonSocial,
    String direccion,
    String zona,
    String materialHabitual,
    Boolean activo
) {}

