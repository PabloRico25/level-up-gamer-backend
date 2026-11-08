package cl.levelupgamer.backend.comun;

/** La operación choca con el estado actual (código repetido, stock insuficiente...). Responde 409. */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}