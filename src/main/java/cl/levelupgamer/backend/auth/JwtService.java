
package cl.levelupgamer.backend.auth;

import cl.levelupgamer.backend.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    public String generarToken(Usuario usuario) {

        Instant ahora = Instant.now();

        JwtClaimsSet datos = JwtClaimsSet.builder()
                .issuer("level-up-gamer")
                .subject(usuario.getCorreo())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(30, ChronoUnit.MINUTES))
                .claim("usuarioId", usuario.getId())
                .claim("tipo", usuario.getTipo().name())
                .build();

        JwsHeader encabezado = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(encabezado, datos)
        ).getTokenValue();
    }
}
