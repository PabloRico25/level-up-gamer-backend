package cl.levelupgamer.backend.boletas;

import cl.levelupgamer.backend.boletas.dto.BoletaResponse;
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

    // GET: Listar todas las boletas
    @GetMapping
    public List<BoletaResponse> listarTodas(
            Authentication authentication) {

        verificarPermisos(authentication);

        return boletaService.listarTodas();
    }

    // GET: Buscar una boleta por ID
    @GetMapping("/{id}")
    public BoletaResponse buscarPorId(
            @PathVariable Long id,
            Authentication authentication) {

        verificarPermisos(authentication);

        return boletaService.buscarPorId(id);
    }

    // GET: Listar boletas de un usuario
    @GetMapping("/usuario/{usuarioId}")
    public List<BoletaResponse> listarPorUsuario(
            @PathVariable Long usuarioId,
            Authentication authentication) {

        verificarPermisos(authentication);

        return boletaService.listarPorUsuario(usuarioId);
    }

    // Verificar que sea administrador o vendedor
    private void verificarPermisos(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión"
            );
        }

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