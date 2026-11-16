
package cl.levelupgamer.backend.boletas;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import cl.levelupgamer.backend.boletas.dto.BoletaResponse;
import cl.levelupgamer.backend.boletas.dto.CrearBoletaRequest;
import cl.levelupgamer.backend.usuarios.Usuario;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@io.swagger.v3.oas.annotations.security.SecurityRequirement(name="bearerAuth")
@RestController
@Tag(name = "Boletas", description = "Compras de los clientes y consulta de órdenes")
@RequestMapping("/api/v1/boletas")
@RequiredArgsConstructor
public class BoletaController {

    private final BoletaService boletaService;
    private final RegistrarCompraService registrarCompraService;
    private final CompradorRepository compradorRepository;

    // Registrar una compra
    @Operation(summary = "Registrar compra",
            description = "Crea la boleta con su detalle, descuenta stock y aplica el 20 % a correos Duoc. Solo CLIENTE y ADMINISTRADOR")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Compra registrada"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida, ítems repetidos o stock insuficiente"),
            @ApiResponse(responseCode = "401", description = "Falta el token"),
            @ApiResponse(responseCode = "403", description = "El rol no puede comprar")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoletaResponse crearCompra(
            @Valid @RequestBody CrearBoletaRequest solicitud,
            Authentication authentication) {

        verificarAutenticacion(authentication);

        boolean puedeComprar = authentication.getAuthorities().stream()
                .anyMatch(rol ->
                        rol.getAuthority().equals("ROLE_CLIENTE")
                                || rol.getAuthority().equals("ROLE_ADMINISTRADOR")
                );

        if (!puedeComprar) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permisos para comprar"
            );
        }

        Usuario usuario = obtenerUsuarioAutenticado(authentication);

        return registrarCompraService.registrar(solicitud, usuario);
    }

    // Consultar las compras del usuario autenticado
    @Operation(summary = "Mis compras",
            description = "Lista las boletas del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Boletas entregadas"),
            @ApiResponse(responseCode = "401", description = "Falta el token")
    })
    @GetMapping("/mis-compras")
    public List<BoletaResponse> misCompras(
            Authentication authentication) {

        Usuario usuario = obtenerUsuarioAutenticado(authentication);

        return boletaService.listarPorUsuario(usuario.getId());
    }

    // Listar todas las boletas
    @Operation(summary = "Listar boletas",
            description = "Lista las órdenes. El cliente solo ve las propias; VENDEDOR y ADMINISTRADOR ven todas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Boletas entregadas"),
            @ApiResponse(responseCode = "401", description = "Falta el token")
    })
    @GetMapping
    public List<BoletaResponse> listarTodas(
            Authentication authentication) {

        Usuario usuario = obtenerUsuarioAutenticado(authentication);
        return esAdministradorOVendedor(authentication) ? boletaService.listarTodas() : boletaService.listarPorUsuario(usuario.getId());
    }

    // Buscar una boleta por ID
    @Operation(summary = "Detalle de boleta",
            description = "Entrega una boleta con su detalle. Un cliente solo puede ver las suyas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Boleta encontrada"),
            @ApiResponse(responseCode = "401", description = "Falta el token"),
            @ApiResponse(responseCode = "403", description = "La boleta pertenece a otro usuario"),
            @ApiResponse(responseCode = "404", description = "No existe la boleta")
    })
    @GetMapping("/{id}")
    public BoletaResponse buscarPorId(
            @PathVariable Long id,
            Authentication authentication) {

        Usuario usuario = obtenerUsuarioAutenticado(authentication);
        return esAdministradorOVendedor(authentication) ? boletaService.buscarPorId(id) : boletaService.buscarPropia(id, usuario.getId());
    }

    // Consultar boletas de un usuario (administración)
    @Operation(summary = "Boletas de un usuario",
            description = "Lista las boletas de un usuario. Un cliente solo puede consultar las suyas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Boletas entregadas"),
            @ApiResponse(responseCode = "401", description = "Falta el token"),
            @ApiResponse(responseCode = "403", description = "Consulta de boletas ajenas")
    })
    @GetMapping("/usuario/{usuarioId}")
    public List<BoletaResponse> listarPorUsuario(
            @PathVariable Long usuarioId,
            Authentication authentication) {

        verificarPermisosConsulta(authentication);

        return boletaService.listarPorUsuario(usuarioId);
    }

    // Obtener el usuario desde la sesión
    private Usuario obtenerUsuarioAutenticado(
            Authentication authentication) {

        verificarAutenticacion(authentication);

        String correo = authentication.getName();

        return compradorRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "No se encontró el usuario autenticado"
                ));
    }

    private boolean esAdministradorOVendedor(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMINISTRADOR") || r.getAuthority().equals("ROLE_VENDEDOR"));
    }

    // Comprobar que exista una sesión
    private void verificarAutenticacion(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión"
            );
        }
    }

    // Permisos para consultar boletas administrativas
    private void verificarPermisosConsulta(
            Authentication authentication) {

        verificarAutenticacion(authentication);

        boolean autorizado = authentication.getAuthorities().stream()
                .anyMatch(rol ->
                        rol.getAuthority().equals("ROLE_ADMINISTRADOR")
                                || rol.getAuthority().equals("ROLE_VENDEDOR")
                );

        if (!autorizado) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permisos para consultar boletas"
            );
        }
    }
}
