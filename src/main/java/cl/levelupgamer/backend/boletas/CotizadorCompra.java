
package cl.levelupgamer.backend.boletas;

import cl.levelupgamer.backend.boletas.dto.CrearBoletaRequest;
import cl.levelupgamer.backend.boletas.dto.DetalleBoletaResponse;
import cl.levelupgamer.backend.boletas.dto.ItemCompraRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CotizadorCompra {

    private final ValidadorCompra validadorCompra;
    private final CalculadoraCompra calculadoraCompra;
    private final DescuentoDuoc descuentoDuoc;

    public ResultadoCompra cotizar(
            CrearBoletaRequest solicitud,
            String correo,
            Map<String, ProductoDisponible> productos) {

        // Validar los datos enviados por el cliente
        validadorCompra.validar(solicitud);

        if (productos == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No hay información de productos disponible"
            );
        }

        List<DetalleBoletaResponse> detalles = new ArrayList<>();
        List<Long> subtotales = new ArrayList<>();

        for (ItemCompraRequest item : solicitud.getItems()) {

            String productoId = item.getProductoId().trim();

            ProductoDisponible producto = productos.get(productoId);

            // Comprobar que el producto exista y esté activo
            if (producto == null || !producto.activo()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no disponible: " + productoId
                );
            }

            // Comprobar que los datos del producto sean válidos
            if (producto.precio() < 0 || producto.stock() < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El producto tiene datos inválidos"
                );
            }

            // Comprobar que haya suficiente stock
            if (producto.stock() < item.getCantidad()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Stock insuficiente para " + productoId
                );
            }

            // Calcular el subtotal de este producto
            long subtotalItem = calculadoraCompra.calcularSubtotalItem(
                    producto.precio(),
                    item.getCantidad()
            );

            detalles.add(new DetalleBoletaResponse(
                    productoId,
                    item.getCantidad(),
                    producto.precio(),
                    subtotalItem
            ));

            subtotales.add(subtotalItem);
        }

        // Calcular los montos finales
        long subtotal = calculadoraCompra.calcularSubtotalCompra(subtotales);
        long descuento = descuentoDuoc.calcular(subtotal, correo);
        long total = calculadoraCompra.calcularTotal(subtotal, descuento);

        return new ResultadoCompra(
                subtotal,
                descuento,
                total,
                List.copyOf(detalles)
        );
    }

    // Datos mínimos de un producto para realizar la cotización
    public record ProductoDisponible(
            long precio,
            int stock,
            boolean activo
    ) {}

    // Resultado de la cotización
    public record ResultadoCompra(
            long subtotal,
            long descuento,
            long total,
            List<DetalleBoletaResponse> items
    ) {}
}
