package pe.upeu.ecorecicla.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CrearPesajeRequest(
    @NotBlank(message = "El material es obligatorio")
    String material,

    @NotNull(message = "El peso bruto es obligatorio")
    @DecimalMin(value = "0.01", message = "El peso bruto debe ser mayor a 0")
    BigDecimal pesoBruto,

    @NotNull(message = "La tara es obligatoria")
    @DecimalMin(value = "0.00", message = "La tara no puede ser negativa")
    BigDecimal tara,

    @Size(max = 250, message = "La observacion no puede exceder 250 caracteres")
    String observacion,

    String operacionId
) {}
