package pe.upeu.ecorecicla.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnularPesajeRequest(
    @NotBlank(message = "El motivo de anulacion es obligatorio")
    @Size(max = 250, message = "El motivo de anulacion no puede exceder 250 caracteres")
    String motivo
) {}

