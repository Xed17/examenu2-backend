package pe.upeu.ecorecicla.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.upeu.ecorecicla.servicio.exception.*;
import pe.upeu.ecorecicla.web.dto.ErrorResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ErrorResponse response = new ErrorResponse(
            LocalDateTime.now().toString(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            "Datos invalidos en la solicitud",
            request.getRequestURI(),
            "VALIDACION_FALLIDA",
            errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ValidacionNegocioException.class)
    public ResponseEntity<ErrorResponse> handleValidacionNegocio(
        ValidacionNegocioException ex,
        HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
            LocalDateTime.now().toString(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getRequestURI(),
            "VALIDACION_NEGOCIO",
            ex.getErrors()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
        BadCredentialsException ex,
        HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
            LocalDateTime.now().toString(),
            HttpStatus.UNAUTHORIZED.value(),
            "Unauthorized",
            ex.getMessage(),
            request.getRequestURI(),
            "CREDENCIALES_INVALIDAS",
            null
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleSpringAccessDenied(
        AccessDeniedException ex,
        HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
            LocalDateTime.now().toString(),
            HttpStatus.FORBIDDEN.value(),
            "Forbidden",
            "No cuenta con los privilegios requeridos para esta operacion",
            request.getRequestURI(),
            "SIN_PRIVILEGIO",
            null
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(AccesoDenegadoNegocioException.class)
    public ResponseEntity<ErrorResponse> handleAccesoDenegadoNegocio(
        AccesoDenegadoNegocioException ex,
        HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
            LocalDateTime.now().toString(),
            HttpStatus.FORBIDDEN.value(),
            "Forbidden",
            ex.getMessage(),
            request.getRequestURI(),
            ex.getCodigo(),
            null
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleRecursoNoEncontrado(
        RecursoNoEncontradoException ex,
        HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
            LocalDateTime.now().toString(),
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getRequestURI(),
            "NO_ENCONTRADO",
            null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ConflictoNegocioException.class)
    public ResponseEntity<ErrorResponse> handleConflictoNegocio(
        ConflictoNegocioException ex,
        HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
            LocalDateTime.now().toString(),
            HttpStatus.CONFLICT.value(),
            "Conflict",
            ex.getMessage(),
            request.getRequestURI(),
            ex.getCodigo(),
            null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
        Exception ex,
        HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
            LocalDateTime.now().toString(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            ex.getMessage() != null ? ex.getMessage() : "Error interno del servidor",
            request.getRequestURI(),
            "ERROR_INTERNO",
            null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

