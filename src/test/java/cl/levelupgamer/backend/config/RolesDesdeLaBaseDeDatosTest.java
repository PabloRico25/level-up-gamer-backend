package cl.levelupgamer.backend.config;

import cl.levelupgamer.backend.usuarios.RolUsuario;
import cl.levelupgamer.backend.usuarios.Usuario;
import cl.levelupgamer.backend.usuarios.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolesDesdeLaBaseDeDatosTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private JwtAuthenticationConverter convertidor;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        convertidor = new SecurityConfig().jwtAuthenticationConverter(usuarioRepository);

        usuario = new Usuario();
        usuario.setId(5L);
        usuario.setCorreo("vendedor@gmail.com");
        usuario.setTipo(RolUsuario.VENDEDOR);
        usuario.setActivo(true);
    }

    private Jwt tokenDe(String correo, Object id, String rolEnElToken) {
        return Jwt.withTokenValue("token-de-prueba")
                .header("alg", "HS256")
                .subject(correo)
                .claim("usuarioId", id)
                .claim("tipo", rolEnElToken)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(1800))
                .build();
    }

    private List<String> roles(Jwt jwt) {
        // Solo se miran los roles; Spring Security también agrega la autoridad FACTOR_BEARER
        return convertidor.convert(jwt).getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(autoridad -> autoridad.startsWith("ROLE_"))
                .toList();
    }

    @Test
    void elRolSeLeeDeLaBaseDeDatosYNoDelToken() {
        when(usuarioRepository.findByCorreo("vendedor@gmail.com")).thenReturn(Optional.of(usuario));

        // El token dice ADMINISTRADOR, pero en la base el usuario es VENDEDOR
        List<String> autoridades = roles(tokenDe("vendedor@gmail.com", 5L, "ADMINISTRADOR"));

        assertEquals(List.of("ROLE_VENDEDOR"), autoridades);
    }

    @Test
    void unCambioDeRolSurteEfectoConElMismoToken() {
        when(usuarioRepository.findByCorreo("vendedor@gmail.com")).thenReturn(Optional.of(usuario));
        Jwt jwt = tokenDe("vendedor@gmail.com", 5L, "VENDEDOR");

        assertEquals(List.of("ROLE_VENDEDOR"), roles(jwt));

        usuario.setTipo(RolUsuario.CLIENTE);

        assertEquals(List.of("ROLE_CLIENTE"), roles(jwt));
    }

    @Test
    void unUsuarioDesactivadoSeRechaza() {
        usuario.setActivo(false);
        when(usuarioRepository.findByCorreo("vendedor@gmail.com")).thenReturn(Optional.of(usuario));

        assertThrows(BadCredentialsException.class,
                () -> convertidor.convert(tokenDe("vendedor@gmail.com", 5L, "VENDEDOR")));
    }

    @Test
    void unTokenDeUnUsuarioQueNoExisteSeRechaza() {
        when(usuarioRepository.findByCorreo("fantasma@gmail.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class,
                () -> convertidor.convert(tokenDe("fantasma@gmail.com", 99L, "CLIENTE")));
    }

    @Test
    void unTokenConElIdDeOtroUsuarioSeRechaza() {
        when(usuarioRepository.findByCorreo("vendedor@gmail.com")).thenReturn(Optional.of(usuario));

        assertThrows(BadCredentialsException.class,
                () -> convertidor.convert(tokenDe("vendedor@gmail.com", 6L, "VENDEDOR")));
    }

    @Test
    void unTokenSinElIdDeUsuarioSeRechaza() {
        when(usuarioRepository.findByCorreo("vendedor@gmail.com")).thenReturn(Optional.of(usuario));

        assertThrows(BadCredentialsException.class,
                () -> convertidor.convert(tokenDe("vendedor@gmail.com", null, "VENDEDOR")));
    }
}
