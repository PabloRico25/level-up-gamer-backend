package cl.levelupgamer.backend.comun;

import cl.levelupgamer.backend.comun.ErrorRespuesta.ErrorCampo;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Convierte cualquier excepción en la respuesta de error común de la API.
 * Extiende ResponseEntityExceptionHandler para que también los errores propios de Spring MVC
 * (ruta inexistente, método no permitido, JSON mal formado...) salgan con el mismo formato.
 */
@RestControllerAdvice
public class ManejadorErrores extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Object> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<Object> solicitudInvalida(SolicitudInvalidaException ex, HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<Object> conflicto(ConflictoException ex, HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI(), null);
    }

    /** Una restricción de la base (clave única, clave foránea) que ningún servicio alcanzó a revisar. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> integridad(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violación de integridad en {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return responder(HttpStatus.CONFLICT, "La operación choca con datos que ya existen o están en uso",
                request.getRequestURI(), null);
    }

    /** Cualquier error no previsto: 500 sin detalles internos. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> inesperado(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado en {}", request.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno", request.getRequestURI(), null);
    }

    /** 400 de validación (@Valid): lista cada campo con su mensaje. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ErrorCampo> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> new ErrorCampo(f.getField(), f.getDefaultMessage()))
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "Hay datos inválidos en la solicitud", rutaDe(request), errores);
    }

    /** Resto de errores de Spring MVC (404 de ruta, 405, 400 por JSON roto...) con el formato común. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        String mensaje = switch (status) {
            case NOT_FOUND -> "No existe la ruta pedida";
            case METHOD_NOT_ALLOWED -> "Método no permitido para esta ruta";
            case BAD_REQUEST -> "La solicitud está mal formada";
            case UNSUPPORTED_MEDIA_TYPE -> "Tipo de contenido no soportado";
            default -> status.getReasonPhrase();
        };
        return responder(status, headers, mensaje, rutaDe(request), null);
    }

    private static String rutaDe(WebRequest request) {
        return request instanceof ServletWebRequest servlet ? servlet.getRequest().getRequestURI() : "";
    }

    private static ResponseEntity<Object> responder(HttpStatus status, String mensaje, String ruta,
                                                    List<ErrorCampo> errores) {
        return responder(status, null, mensaje, ruta, errores);
    }

    /** Con "headers" se conservan las cabeceras que Spring ya preparó (Allow en un 405, Accept en un 415). */
    private static ResponseEntity<Object> responder(HttpStatus status, HttpHeaders headers, String mensaje,
                                                    String ruta, List<ErrorCampo> errores) {
        ErrorRespuesta cuerpo = new ErrorRespuesta(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                mensaje, ruta, errores);
        ResponseEntity.BodyBuilder respuesta = ResponseEntity.status(status);
        if (headers != null) {
            respuesta.headers(headers);
        }
        return respuesta.body(cuerpo);
    }
}