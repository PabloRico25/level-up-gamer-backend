package cl.levelupgamer.backend.categorias.dto;

import cl.levelupgamer.backend.categorias.Categoria;

/** Categoría tal como se entrega por la API: { "id": 1, "nombre": "Juegos de Mesa" }. */
public record CategoriaResponse(Long id, String nombre) {

    public static CategoriaResponse desde(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNombre());
    }
}