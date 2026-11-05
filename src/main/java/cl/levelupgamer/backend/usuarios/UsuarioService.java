
package cl.levelupgamer.backend.usuarios;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long registrar(RegistroRequest datos) {

        String correo = datos.getCorreo()
                .trim()
                .toLowerCase(Locale.ROOT);

        String run = datos.getRun()
                .replace(".", "")
                .replace("-", "")
                .trim()
                .toUpperCase(Locale.ROOT);

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new IllegalArgumentException(
                    "El correo ya está registrado"
            );
        }

        if (usuarioRepository.existsByRun(run)) {
            throw new IllegalArgumentException(
                    "El RUN ya está registrado"
            );
        }

        if (datos.getFechaNacimiento()
                .isAfter(LocalDate.now().minusYears(18))) {
            throw new IllegalArgumentException(
                    "Debes ser mayor de 18 años"
            );
        }

        Usuario usuario = new Usuario();

        usuario.setRun(run);
        usuario.setNombre(datos.getNombre().trim());
        usuario.setApellidos(datos.getApellidos().trim());
        usuario.setCorreo(correo);

        usuario.setContrasenaHash(
                passwordEncoder.encode(datos.getContrasena())
        );

        usuario.setFechaNacimiento(datos.getFechaNacimiento());
        usuario.setRegion(datos.getRegion().trim());
        usuario.setComuna(datos.getComuna().trim());
        usuario.setDireccion(datos.getDireccion().trim());

        usuario.setTipo(RolUsuario.CLIENTE);

        Usuario guardado = usuarioRepository.save(usuario);

        return guardado.getId();
    }
}
