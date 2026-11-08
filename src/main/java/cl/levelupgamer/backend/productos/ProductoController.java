package cl.levelupgamer.backend.productos;

import cl.levelupgamer.backend.productos.dto.ProductoRequest;
import cl.levelupgamer.backend.productos.dto.ProductoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Productos. Leer es público; crear, editar y desactivar es solo para ADMINISTRADOR.
 * El permiso de cada ruta lo declara SecurityConfig, no este controlador.
 */
@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Catálogo de productos de la tienda")
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    @Operation(summary = "Listar productos", description = "Entrega los productos activos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Productos entregados (la lista puede venir vacía)")
    })
    public ResponseEntity<List<ProductoResponse>> listar() {
        return ResponseEntity.ok(productoService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar producto por código",
            description = "Entrega el detalle de un producto activo a partir de su código, por ejemplo JM001")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un producto activo con ese código")
    })
    public ResponseEntity<ProductoResponse> buscarPorId(
            @Parameter(description = "Código del producto (por ejemplo JM001)", required = true) @PathVariable String id) {
        return ResponseEntity.ok(productoService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear producto",
            description = "Crea un producto nuevo; el código lo define quien lo crea. Solo ADMINISTRADOR",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (campos incorrectos o categoría inexistente)"),
            @ApiResponse(responseCode = "401", description = "Falta el token"),
            @ApiResponse(responseCode = "403", description = "El usuario no es ADMINISTRADOR"),
            @ApiResponse(responseCode = "409", description = "Ya existe un producto con ese código")
    })
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        ProductoResponse creado = productoService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar producto",
            description = "Edita un producto activo; el código de la ruta manda. Solo ADMINISTRADOR",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (campos incorrectos o categoría inexistente)"),
            @ApiResponse(responseCode = "401", description = "Falta el token"),
            @ApiResponse(responseCode = "403", description = "El usuario no es ADMINISTRADOR"),
            @ApiResponse(responseCode = "404", description = "No existe un producto activo con ese código")
    })
    public ResponseEntity<ProductoResponse> actualizar(
            @Parameter(description = "Código del producto (por ejemplo JM001)", required = true) @PathVariable String id,
            @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(productoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar producto",
            description = "No borra la fila: deja el producto desactivado para no romper las boletas antiguas. Solo ADMINISTRADOR",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto desactivado"),
            @ApiResponse(responseCode = "401", description = "Falta el token"),
            @ApiResponse(responseCode = "403", description = "El usuario no es ADMINISTRADOR"),
            @ApiResponse(responseCode = "404", description = "No existe un producto activo con ese código")
    })
    public ResponseEntity<Void> desactivar(
            @Parameter(description = "Código del producto (por ejemplo JM001)", required = true) @PathVariable String id) {
        productoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}