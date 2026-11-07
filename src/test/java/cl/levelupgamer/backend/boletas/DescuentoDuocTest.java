package cl.levelupgamer.backend.boletas;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DescuentoDuocTest {

    private final DescuentoDuoc descuentoDuoc = new DescuentoDuoc();

    @Test
    void aplicarDescuentoEstudianteDuoc() {
        long descuento = descuentoDuoc.calcular(
                50000L, "gabo@duoc.cl"
        );

        assertEquals(10000L, descuento);
    }

    @Test
    void aplicarDescuentoProfesorDuoc() {
        long descuento = descuentoDuoc.calcular(
                100000L, "profesor@profesor.duoc.cl"
        );

        assertEquals(20000L, descuento);
    }

    @Test
    void noAplicarDescuentoCorreoNormal() {
        long descuento = descuentoDuoc.calcular(
                50000L, "gabo@gmail.com"
        );

        assertEquals(0L, descuento);
    }

    @Test
    void reconocerCorreoConMayusculas() {
        long descuento = descuentoDuoc.calcular(
                50000L, "GABO@DUOC.CL"
        );

        assertEquals(10000L, descuento);
    }

    @Test
    void rechazarDominioFalso() {
        long descuento = descuentoDuoc.calcular(
                50000L, "gabo@duoc.cl.falso.com"
        );

        assertEquals(0L, descuento);
    }
}