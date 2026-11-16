
package cl.levelupgamer.backend.auth;

import cl.levelupgamer.backend.usuarios.Usuario;
import cl.levelupgamer.backend.usuarios.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) {

        String correoNormalizado = correo.trim()
                .toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository
                .findByCorreo(correoNormalizado)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Credenciales incorrectas"
                ));

        return User.builder()
                .username(usuario.getCorreo())
                .password(usuario.getContrasenaHash())
                .disabled(!usuario.isActivo())
                .authorities("ROLE_" + usuario.getTipo().name())
                .build();
    }
}
