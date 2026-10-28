package pe.upeu.ecorecicla.web.dto;

import java.math.BigDecimal;

public record PesajeResponse(
    Long id,
    Long rutaPuntoId,
    Long puntoId,
    String fechaHora,
    String material,
    BigDecimal pesoBruto,
    BigDecimal tara,
    BigDecimal pesoNeto,
    String observacion,
    Long operarioId,
    String operarioNombre,
    String estado,
    String motivoAnulacion,
    Long version
) {}
