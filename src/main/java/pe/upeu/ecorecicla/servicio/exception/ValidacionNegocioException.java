package pe.upeu.ecorecicla.servicio.exception;

import java.util.Map;

public class ValidacionNegocioException extends RuntimeException {
    private final Map<String, String> errors;

    public ValidacionNegocioException(String campo, String message) {
        super(message);
        this.errors = Map.of(campo, message);
    }

    public ValidacionNegocioException(Map<String, String> errors, String message) {
        super(message);
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}

