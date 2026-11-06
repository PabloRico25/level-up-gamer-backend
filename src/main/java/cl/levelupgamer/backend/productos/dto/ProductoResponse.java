package cl.levelupgamer.backend.productos.dto;

import cl.levelupgamer.backend.productos.Producto;

/**
 * Producto tal como se entrega por la API (CONTRATO_API.md, sección 3).
 * categoriaId es la relación y categoria el nombre que muestra el frontend; stockCritico puede venir null.
 */
public record ProductoResponse(
        String id,
        String nombre,
        Long categoriaId,
        String categoria,
        Long precio,
        Integer stock,
        Integer stockCritico,
        String fabricante,
        String imagen,
        String descripcion) {

    public static ProductoResponse desde(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getCategoria().getId(),
                producto.getCategoria().getNombre(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getStockCritico(),
                producto.getFabricante(),
                producto.getImagen(),
                producto.getDescripcion());
    }
}