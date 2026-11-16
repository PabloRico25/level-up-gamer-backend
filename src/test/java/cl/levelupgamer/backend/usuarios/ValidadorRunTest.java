package cl.levelupgamer.backend.usuarios;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorRunTest {

    @Test
    void aceptaUnRunConDigitoVerificadorCorrecto() {
        assertTrue(ValidadorRun.esValido("11111111-1"));
        assertTrue(ValidadorRun.esValido("22222222-2"));
    }

    @Test
    void aceptaElRunConPuntosOSinGuion() {
        assertTrue(ValidadorRun.esValido("11.111.111-1"));
        assertTrue(ValidadorRun.esValido("111111111"));
    }

    @Test
    void rechazaUnDigitoVerificadorIncorrecto() {
        assertFalse(ValidadorRun.esValido("11111111-2"));
    }

    @Test
    void rechazaUnRunNuloOConFormatoInvalido() {
        assertFalse(ValidadorRun.esValido(null));
        assertFalse(ValidadorRun.esValido(""));
        assertFalse(ValidadorRun.esValido("abc"));
        assertFalse(ValidadorRun.esValido("123"));
    }
}
