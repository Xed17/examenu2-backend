package pe.upeu.ecorecicla.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OmisionRequest(
    @NotBlank(message = "El motivo de omision es obligatorio")
    @Size(max = 250, message = "El motivo de omision no puede exceder 250 caracteres")
    String motivo
) {}

