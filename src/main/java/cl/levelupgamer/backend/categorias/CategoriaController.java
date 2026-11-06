package cl.levelupgamer.backend.categorias;

import cl.levelupgamer.backend.categorias.dto.CategoriaRequest;
import cl.levelupgamer.backend.categorias.dto.CategoriaResponse;
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
 * Categorías. Leer es público; crear, editar y eliminar es solo para ADMINISTRADOR.
 * El permiso de cada ruta lo declara SecurityConfig, no este controlador.
 */
@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "Operaciones sobre las categorías del catálogo")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    @Operation(summary = "Listar categorías", description = "Entrega todas las categorías, ordenadas por id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categorías entregadas")
    })
    public ResponseEntity<List<CategoriaResponse>> listar() {
        return ResponseEntity.ok(categoriaService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoría por id", description = "Entrega una categoría a partir de su id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe una categoría con ese id")
    })
    public ResponseEntity<CategoriaResponse> buscarPorId(
            @Parameter(description = "Id de la categoría", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear categoría", description = "Crea una categoría nueva. Solo ADMINISTRADOR",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoría creada"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "Falta el token"),
            @ApiResponse(responseCode = "403", description = "El usuario no es ADMINISTRADOR"),
            @ApiResponse(responseCode = "409", description = "Ya existe una categoría con ese nombre")
    })
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest request) {
        CategoriaResponse creada = categoriaService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(creada.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar categoría", description = "Cambia el nombre de una categoría. Solo ADMINISTRADOR",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría actualizada"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "Falta el token"),
            @ApiResponse(responseCode = "403", description = "El usuario no es ADMINISTRADOR"),
            @ApiResponse(responseCode = "404", description = "No existe una categoría con ese id"),
            @ApiResponse(responseCode = "409", description = "Ya existe otra categoría con ese nombre")
    })
    public ResponseEntity<CategoriaResponse> actualizar(
            @Parameter(description = "Id de la categoría", required = true) @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.ok(categoriaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría que no tenga productos. Solo ADMINISTRADOR",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoría eliminada"),
            @ApiResponse(responseCode = "401", description = "Falta el token"),
            @ApiResponse(responseCode = "403", description = "El usuario no es ADMINISTRADOR"),
            @ApiResponse(responseCode = "404", description = "No existe una categoría con ese id"),
            @ApiResponse(responseCode = "409", description = "La categoría tiene productos")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Id de la categoría", required = true) @PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
