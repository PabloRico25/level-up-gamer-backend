
package cl.levelupgamer.backend.boletas;

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

@RestController
@RequestMapping("/api/v1/boletas")
@RequiredArgsConstructor
public class BoletaController {

    private final BoletaService boletaService;
    private final RegistrarCompraService registrarCompraService;
    private final CompradorRepository compradorRepository;

    // Registrar una compra
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
    @GetMapping("/mis-compras")
    public List<BoletaResponse> misCompras(
            Authentication authentication) {

        Usuario usuario = obtenerUsuarioAutenticado(authentication);

        return boletaService.listarPorUsuario(usuario.getId());
    }

    // Listar todas las boletas
    @GetMapping
    public List<BoletaResponse> listarTodas(
            Authentication authentication) {

        verificarPermisosConsulta(authentication);

        return boletaService.listarTodas();
    }

    // Buscar una boleta por ID
    @GetMapping("/{id}")
    public BoletaResponse buscarPorId(
            @PathVariable Long id,
            Authentication authentication) {

        verificarPermisosConsulta(authentication);

        return boletaService.buscarPorId(id);
    }

    // Consultar boletas de un usuario (administración)
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
