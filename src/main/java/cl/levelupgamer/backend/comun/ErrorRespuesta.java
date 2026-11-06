package cl.levelupgamer.backend.comun;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Forma única de todas las respuestas de error de la API (CONTRATO_API.md, sección 6).
 * "errores" solo aparece en los 400 de validación.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorRespuesta(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<ErrorCampo> errores) {

    public record ErrorCampo(String campo, String mensaje) {
    }
}