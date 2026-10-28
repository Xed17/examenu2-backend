package pe.upeu.ecorecicla.web.dto;

import java.util.Map;

public record ErrorResponse(
    String timestamp,
    int status,
    String error,
    String message,
    String path,
    String codigo,
    Map<String, String> validationErrors
) {}

