
package cl.levelupgamer.backend.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Autenticación", description = "Inicio y cierre de sesión, renovación y perfil del usuario")
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String COOKIE_NOMBRE = "levelup_refresh";

    @Value("${app.cors.allowed-origins}")
    private Set<String> origenesPermitidos;

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final TokenRenovacionService tokenRenovacionService;

    @Value("${app.auth.cookie-secure:false}")
    private boolean cookieSecure;

    // Iniciar sesión
    @Operation(summary = "Iniciar sesión",
            description = "Valida correo y contraseña. Entrega un JWT de acceso (30 minutos) y deja la cookie HttpOnly de renovación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sesión iniciada"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas o usuario inactivo")
    })
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
    @Operation(summary = "Perfil del usuario autenticado",
            description = "Entrega los datos del usuario dueño del token",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil entregado"),
            @ApiResponse(responseCode = "401", description = "Falta el token o no es válido")
    })
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
    @Operation(summary = "Renovar sesión",
            description = "Con la cookie de renovación entrega un JWT nuevo y rota la cookie. Rechaza orígenes no permitidos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token renovado"),
            @ApiResponse(responseCode = "401", description = "Cookie ausente, vencida o ya usada"),
            @ApiResponse(responseCode = "403", description = "Origen no permitido")
    })
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
    @Operation(summary = "Cerrar sesión",
            description = "Revoca la cookie de renovación y la elimina del navegador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Sesión cerrada"),
            @ApiResponse(responseCode = "403", description = "Origen no permitido")
    })
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
                !origenesPermitidos.contains(origin) ||
                !"XMLHttpRequest".equals(solicitadoCon)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solicitud no autorizada"
            );
        }
    }
}
