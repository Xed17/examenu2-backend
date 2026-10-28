package pe.upeu.ecorecicla.servicio.exception;

public class ConflictoNegocioException extends RuntimeException {
    private final String codigo;

    public ConflictoNegocioException(String codigo, String message) {
        super(message);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}

