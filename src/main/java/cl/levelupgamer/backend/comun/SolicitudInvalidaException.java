package cl.levelupgamer.backend.comun;

/** Los datos enviados no cumplen una regla de negocio. Responde 400. */
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}