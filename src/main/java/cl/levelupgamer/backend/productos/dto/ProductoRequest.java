package cl.levelupgamer.backend.productos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos que llegan para crear o editar un producto. No lleva "categoria" (el nombre lo pone el backend a partir de categoriaId).
 * Al crear, el id (código) es obligatorio; al editar, manda el id de la ruta y el de este cuerpo se ignora.
 * Las medidas son las mismas del formulario de administración del frontend.
 */
public record ProductoRequest(
        @Pattern(regexp = "^[A-Z0-9-]{3,20}$", message = "Código de 3 a 20 caracteres: letras mayúsculas, números o guion")
        String id,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "Máximo 100 caracteres")
        String nombre,

        @NotNull(message = "La categoría es obligatoria")
        Long categoriaId,

        @NotNull(message = "El precio es obligatorio")
        @Min(value = 1, message = "Debe ser mayor que 0")
        Long precio,

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "No puede ser negativo")
        Integer stock,

        @Min(value = 0, message = "No puede ser negativo")
        Integer stockCritico,

        @NotBlank(message = "El fabricante es obligatorio")
        @Size(max = 100, message = "Máximo 100 caracteres")
        String fabricante,

        @Size(max = 255, message = "Máximo 255 caracteres")
        String imagen,

        @Size(max = 500, message = "Máximo 500 caracteres")
        String descripcion) {
}