
package cl.levelupgamer.backend.auth;

import cl.levelupgamer.backend.usuarios.Usuario;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class TokenRenovacionService {

    private final TokenRenovacionRepository tokenRepository;
    private final JwtService jwtService;

    private final SecureRandom random = new SecureRandom();

    // Crear un token de renovación
    public String crearToken(Usuario usuario) {

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        TokenRenovacion registro = new TokenRenovacion();

        registro.setUsuario(usuario);
        registro.setTokenHash(calcularHash(token));
        registro.setFechaExpiracion(
                Instant.now().plus(7, ChronoUnit.DAYS)
        );
        registro.setRevocado(false);

        tokenRepository.save(registro);

        return token;
    }

    // Renovar una sesión
    @Transactional
    public TokensRenovados renovar(String tokenRecibido) {

        if (tokenRecibido == null || tokenRecibido.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Token de renovación no válido"
            );
        }

        TokenRenovacion registro = tokenRepository
                .findByTokenHash(calcularHash(tokenRecibido))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Token de renovación no válido"
                ));

        if (!registro.getUsuario().isActivo() || registro.isRevocado() ||
                !registro.getFechaExpiracion().isAfter(Instant.now())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Token vencido o revocado"
            );
        }

        // Invalidar el token anterior
        registro.setRevocado(true);
        tokenRepository.save(registro);

        // Crear tokens nuevos
        String nuevoRefresh = crearToken(registro.getUsuario());

        String nuevoAccess = jwtService.generarToken(
                registro.getUsuario()
        );

        return new TokensRenovados(
                nuevoAccess,
                nuevoRefresh
        );
    }

    // Revocar un token al cerrar sesión
    @Transactional
    public void revocar(String tokenRecibido) {

        if (tokenRecibido == null || tokenRecibido.isBlank()) {
            return;
        }

        tokenRepository
                .findByTokenHash(calcularHash(tokenRecibido))
                .ifPresent(registro -> {
                    registro.setRevocado(true);
                    tokenRepository.save(registro);
                });
    }

    // Guardar solo el hash del token
    private String calcularHash(String token) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] resultado = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(resultado);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "No se pudo calcular el hash",
                    e
            );
        }
    }

    public record TokensRenovados(
            String accessToken,
            String refreshToken
    ) {
    }
}
