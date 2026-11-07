package cl.levelupgamer.backend.boletas;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class CalculadoraCompra {

    // Calcula el precio de un producto según su cantidad
    public int calcularSubtotalItem(int precioUnitario, int cantidad) {

        if (precioUnitario < 0 || cantidad < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El precio o la cantidad no son válidos"
            );
        }

        long subtotal = (long) precioUnitario * cantidad;

        if (subtotal > Integer.MAX_VALUE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El subtotal supera el máximo permitido"
            );
        }

        return (int) subtotal;
    }

    // Suma los subtotales de todos los productos
    public int calcularSubtotalCompra(List<Integer> subtotales) {

        if (subtotales == null || subtotales.isEmpty()
                || subtotales.stream().anyMatch(s -> s == null || s < 0)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los subtotales no son válidos"
            );
        }

        long subtotal = 0;

        for (Integer valor : subtotales) {
            subtotal += valor;
        }

        if (subtotal > Integer.MAX_VALUE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La compra supera el máximo permitido"
            );
        }

        return (int) subtotal;
    }

    // Calcula el total después de aplicar un descuento
    public int calcularTotal(int subtotal, int descuento) {

        if (subtotal < 0 || descuento < 0 || descuento > subtotal) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El descuento no es válido"
            );
        }

        return subtotal - descuento;
    }
}