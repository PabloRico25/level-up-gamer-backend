package cl.levelupgamer.backend.boletas;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class CalculadoraCompra {

    public long calcularSubtotalItem(long precioUnitario, int cantidad) {

        if (precioUnitario < 0 || cantidad < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El precio o la cantidad no son válidos"
            );
        }

        try {
            return Math.multiplyExact(precioUnitario, (long) cantidad);
        } catch (ArithmeticException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El subtotal supera el máximo permitido"
            );
        }
    }

    public long calcularSubtotalCompra(List<Long> subtotales) {

        if (subtotales == null || subtotales.isEmpty()
                || subtotales.stream().anyMatch(s -> s == null || s < 0)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los subtotales no son válidos"
            );
        }

        long total = 0L;

        try {
            for (Long subtotal : subtotales) {
                total = Math.addExact(total, subtotal);
            }
        } catch (ArithmeticException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La compra supera el máximo permitido"
            );
        }

        return total;
    }

    public long calcularTotal(long subtotal, long descuento) {

        if (subtotal < 0 || descuento < 0 || descuento > subtotal) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El descuento no es válido"
            );
        }

        return subtotal - descuento;
    }
}