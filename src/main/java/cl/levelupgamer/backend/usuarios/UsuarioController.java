
package cl.levelupgamer.backend.usuarios;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    // Listar usuarios
    @GetMapping
    @Transactional(readOnly = true)
    public List<UsuarioResumen> listarUsuarios() {

        return usuarioRepository.findAll()
                .stream()
                .map(this::convertirResumen)
                .toList();
    }

    // Cambiar el rol de un usuario
    @PatchMapping("/{id}/rol")
    @Transactional
    public UsuarioResumen cambiarRol(
            @PathVariable Long id,
            @Valid @RequestBody CambiarRolRequest datos,
            @AuthenticationPrincipal Jwt jwt
    ) {

        // Comprobar que quien realiza el cambio
        // sigue siendo administrador en MySQL
        Usuario administrador = usuarioRepository
                .findByCorreo(jwt.getSubject())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario no encontrado"
                ));

        if (administrador.getTipo() != RolUsuario.ADMINISTRADOR) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permisos para cambiar roles"
            );
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

        // Evitar que el administrador
        // se quite sus propios permisos
        if (
                administrador.getId().equals(usuario.getId())
                        && datos.tipo() != RolUsuario.ADMINISTRADOR
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No puedes quitarte tu propio rol de administrador"
            );
        }

        usuario.setTipo(datos.tipo());

        usuarioRepository.save(usuario);

        return convertirResumen(usuario);
    }

    // Datos recibidos para cambiar el rol
    public record CambiarRolRequest(
            @NotNull RolUsuario tipo
    ) {
    }

    // Datos que enviamos a React
    public record UsuarioResumen(
            Long id,
            String run,
            String nombre,
            String apellidos,
            String correo,
            RolUsuario tipo
    ) {
    }

    private UsuarioResumen convertirResumen(Usuario usuario) {

        return new UsuarioResumen(
                usuario.getId(),
                usuario.getRun(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getCorreo(),
                usuario.getTipo()
        );
    }
}
