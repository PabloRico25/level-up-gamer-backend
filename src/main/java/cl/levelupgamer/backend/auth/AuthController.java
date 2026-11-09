
package cl.levelupgamer.backend.auth;

import cl.levelupgamer.backend.usuarios.Usuario;
import cl.levelupgamer.backend.usuarios.UsuarioRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String COOKIE_NOMBRE = "levelup_refresh";

    private static final Set<String> ORIGENES_PERMITIDOS = Set.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173"
    );

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final TokenRenovacionService tokenRenovacionService;

    @Value("${app.auth.cookie-secure:false}")
    private boolean cookieSecure;

    // Iniciar sesión
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest datos) {

        String correo = datos.getCorreo()
                .trim()
                .toLowerCase(Locale.ROOT);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            correo,
                            datos.getContrasena()
                    )
            );

        } catch (AuthenticationException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Correo o contraseña incorrectos"
            );
        }

        Usuario usuario = usuarioRepository
                .findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Correo o contraseña incorrectos"
                ));

        String accessToken = jwtService.generarToken(usuario);

        String refreshToken =
                tokenRenovacionService.crearToken(usuario);

        LoginResponse respuesta = new LoginResponse(
                accessToken,
                "Bearer",
                new LoginResponse.UsuarioResumen(
                        usuario.getId(),
                        usuario.getNombre(),
                        usuario.getCorreo(),
                        usuario.getTipo()
                )
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        crearCookie(refreshToken).toString()
                )
                .body(respuesta);
    }

    // Consultar usuario conectado
    @GetMapping("/me")
    public LoginResponse.UsuarioResumen miPerfil(
            @AuthenticationPrincipal Jwt jwt) {

        Usuario usuario = usuarioRepository
                .findByCorreo(jwt.getSubject())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario no encontrado"
                ));

        return new LoginResponse.UsuarioResumen(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getTipo()
        );
    }

    // Renovar el JWT
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> renovar(
            @RequestHeader(value = "Origin", required = false)
            String origin,

            @RequestHeader(
                    value = "X-Requested-With",
                    required = false
            )
            String solicitadoCon,

            @CookieValue(
                    name = COOKIE_NOMBRE,
                    required = false
            )
            String refreshToken) {

        validarSolicitud(origin, solicitadoCon);

        TokenRenovacionService.TokensRenovados tokens =
                tokenRenovacionService.renovar(refreshToken);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        crearCookie(tokens.refreshToken()).toString()
                )
                .body(Map.of(
                        "accessToken", tokens.accessToken(),
                        "tokenType", "Bearer"
                ));
    }

    // Cerrar sesión
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Origin", required = false)
            String origin,

            @RequestHeader(
                    value = "X-Requested-With",
                    required = false
            )
            String solicitadoCon,

            @CookieValue(
                    name = COOKIE_NOMBRE,
                    required = false
            )
            String refreshToken) {

        validarSolicitud(origin, solicitadoCon);

        tokenRenovacionService.revocar(refreshToken);

        return ResponseEntity.noContent()
                .header(
                        HttpHeaders.SET_COOKIE,
                        eliminarCookie().toString()
                )
                .build();
    }

    // Crear cookie de renovación
    private ResponseCookie crearCookie(String token) {

        return ResponseCookie.from(COOKIE_NOMBRE, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(Duration.ofDays(7))
                .build();
    }

    // Eliminar cookie al cerrar sesión
    private ResponseCookie eliminarCookie() {

        return ResponseCookie.from(COOKIE_NOMBRE, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(Duration.ZERO)
                .build();
    }

    // Comprobar el origen y la cabecera
    private void validarSolicitud(
            String origin,
            String solicitadoCon) {

        if (origin == null ||
                !ORIGENES_PERMITIDOS.contains(origin) ||
                !"XMLHttpRequest".equals(solicitadoCon)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solicitud no autorizada"
            );
        }
    }
}
