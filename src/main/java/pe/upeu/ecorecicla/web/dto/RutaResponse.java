package pe.upeu.ecorecicla.web.dto;

import java.util.List;

public record RutaResponse(
    Long id,
    String fecha,
    String estado,
    String cerradaEn,
    String cerradaPor,
    Long version,
    List<RutaPuntoResponse> puntos
) {}

