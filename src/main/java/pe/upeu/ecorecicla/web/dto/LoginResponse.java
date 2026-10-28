package pe.upeu.ecorecicla.web.dto;

import java.util.List;

public record LoginResponse(
    String token,
    String usuario,
    String nombre,
    String rol,
    List<String> privilegios,
    String expiraEn
) {}

