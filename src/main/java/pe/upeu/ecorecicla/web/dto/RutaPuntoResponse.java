package pe.upeu.ecorecicla.web.dto;

public record RutaPuntoResponse(
    Long id,
    Long rutaId,
    Long puntoId,
    String razonSocial,
    String direccion,
    String zona,
    String materialHabitual,
    Integer secuencia,
    Boolean atendido,
    String motivoOmision
) {}

