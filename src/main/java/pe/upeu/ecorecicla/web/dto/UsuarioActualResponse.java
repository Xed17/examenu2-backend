package pe.upeu.ecorecicla.web.dto;

import java.util.List;

public record UsuarioActualResponse(
    Long id,
    String username,
    String nombre,
    List<String> roles,
    List<String> privilegios
) {}

