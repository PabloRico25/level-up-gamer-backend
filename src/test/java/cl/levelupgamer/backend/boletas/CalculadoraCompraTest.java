package cl.levelupgamer.backend.boletas;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalculadoraCompraTest {

    private final CalculadoraCompra calculadora =
            new CalculadoraCompra();

    @Test
    void calcularSubtotalItemCorrectamente() {

        long resultado = calculadora.calcularSubtotalItem(25000L, 2);

        assertEquals(50000L, resultado);
    }

    @Test
    void rechazarCantidadNegativa() {

        assertThrows(
                ResponseStatusException.class,
                () -> calculadora.calcularSubtotalItem(25000L, -1)
        );
    }

    @Test
    void calcularSubtotalCompraCorrectamente() {

        long resultado = calculadora.calcularSubtotalCompra(
                List.of(50000L, 15000L)
        );

        assertEquals(65000L, resultado);
    }

    @Test
    void calcularTotalConDescuento() {

        long resultado = calculadora.calcularTotal(65000L, 5000L);

        assertEquals(60000L, resultado);
    }

    @Test
    void rechazarDescuentoMayorAlSubtotal() {

        assertThrows(
                ResponseStatusException.class,
                () -> calculadora.calcularTotal(10000L, 15000L)
        );
    }
}