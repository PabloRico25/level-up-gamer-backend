package cl.levelupgamer.backend.boletas;

import cl.levelupgamer.backend.boletas.dto.CrearBoletaRequest;
import cl.levelupgamer.backend.boletas.dto.ItemCompraRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;

@Component
public class ValidadorCompra {

    public void validar(CrearBoletaRequest solicitud) {

        if (solicitud == null
                || solicitud.getItems() == null
                || solicitud.getItems().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La compra debe contener al menos un producto"
            );
        }

        Set<String> productosRevisados = new HashSet<>();

        for (ItemCompraRequest item : solicitud.getItems()) {

            if (item == null
                    || item.getProductoId() == null
                    || item.getProductoId().isBlank()) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El producto es obligatorio"
                );
            }

            if (item.getCantidad() == null || item.getCantidad() < 1) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La cantidad debe ser mayor a cero"
                );
            }

            String productoId = item.getProductoId().trim();

            if (!productosRevisados.add(productoId)) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No puedes repetir el mismo producto en la compra"
                );
            }
        }
    }
}