
package cl.levelupgamer.backend.boletas;

public interface InventarioCompra {

    // Obtener el precio, stock y estado de un producto
    CotizadorCompra.ProductoDisponible buscarProducto(
            String productoId
    );

    // Descontar las unidades vendidas
    void descontarStock(
            String productoId,
            int cantidad
    );
}
