
package cl.levelupgamer.backend.usuarios;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long registrar(RegistroRequest datos) {

        // Limpiar RUN y correo
        String run = datos.getRun()
                .replace(".", "")
                .replace("-", "")
                .trim()
                .toUpperCase(Locale.ROOT);

        String correo = datos.getCorreo()
                .trim()
                .toLowerCase(Locale.ROOT);

        // Validar RUN chileno
        if (!ValidadorRun.esValido(run)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El RUN ingresado no es válido"
            );
        }

        // Evitar RUN duplicado
        if (usuarioRepository.existsByRun(run)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este RUN ya está registrado"
            );
        }

        // Evitar correo duplicado
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este correo ya está registrado"
            );
        }

        // Comprobar mayoría de edad
        LocalDate fechaNacimiento = datos.getFechaNacimiento();

        if (fechaNacimiento == null ||
                fechaNacimiento.plusYears(18).isAfter(LocalDate.now())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes tener al menos 18 años"
            );
        }

        if (datos.getContrasena().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La contraseña supera 72 bytes UTF-8");
        }
        // Crear usuario
        Usuario usuario = new Usuario();

        usuario.setRun(run);
        usuario.setNombre(datos.getNombre().trim());
        usuario.setApellidos(datos.getApellidos().trim());
        usuario.setCorreo(correo);

        // Guardar contraseña protegida con BCrypt
        usuario.setContrasenaHash(
                passwordEncoder.encode(datos.getContrasena())
        );

        usuario.setFechaNacimiento(fechaNacimiento);
        usuario.setRegion(datos.getRegion());
        usuario.setComuna(datos.getComuna());
        usuario.setDireccion(datos.getDireccion().trim());

        // Todo usuario nuevo comienza como CLIENTE
        usuario.setTipo(RolUsuario.CLIENTE);

        // Guardar en MySQL
        Usuario guardado = usuarioRepository.save(usuario);

        return guardado.getId();
    }
}
