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

        int resultado = calculadora.calcularSubtotalItem(25000, 2);

        assertEquals(50000, resultado);
    }

    @Test
    void rechazarCantidadNegativa() {

        assertThrows(
                ResponseStatusException.class,
                () -> calculadora.calcularSubtotalItem(25000, -1)
        );
    }

    @Test
    void calcularSubtotalCompraCorrectamente() {

        int resultado = calculadora.calcularSubtotalCompra(
                List.of(50000, 15000)
        );

        assertEquals(65000, resultado);
    }

    @Test
    void calcularTotalConDescuento() {

        int resultado = calculadora.calcularTotal(65000, 5000);

        assertEquals(60000, resultado);
    }

    @Test
    void rechazarDescuentoMayorAlSubtotal() {

        assertThrows(
                ResponseStatusException.class,
                () -> calculadora.calcularTotal(10000, 15000)
        );
    }
}