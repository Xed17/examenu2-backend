package pe.upeu.ecorecicla.servicio.exception;

public class AccesoDenegadoNegocioException extends RuntimeException {
    private final String codigo;

    public AccesoDenegadoNegocioException(String codigo, String message) {
        super(message);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}

