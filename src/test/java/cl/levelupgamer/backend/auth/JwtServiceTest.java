package cl.levelupgamer.backend.auth;

import cl.levelupgamer.backend.usuarios.RolUsuario;
import cl.levelupgamer.backend.usuarios.Usuario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final byte[] CLAVE = "clave-de-prueba-de-32-bytes-0123".getBytes();

    private JwtEncoder encoder;
    private JwtDecoder decoder;
    private JwtService jwtService;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        SecretKey clave = new SecretKeySpec(CLAVE, "HmacSHA256");

        encoder = NimbusJwtEncoder.withSecretKey(clave).algorithm(MacAlgorithm.HS256).build();
        decoder = decodificador(clave);
        jwtService = new JwtService(encoder);

        usuario = new Usuario();
        usuario.setId(7L);
        usuario.setCorreo("cliente@duoc.cl");
        usuario.setTipo(RolUsuario.CLIENTE);
    }

    private JwtDecoder decodificador(SecretKey clave) {
        NimbusJwtDecoder nuevo = NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
        nuevo.setJwtValidator(JwtValidators.createDefaultWithIssuer("level-up-gamer"));
        return nuevo;
    }

    @Test
    void elTokenTraeElCorreoElIdYElRolDelUsuario() {
        Jwt jwt = decoder.decode(jwtService.generarToken(usuario));

        assertEquals("cliente@duoc.cl", jwt.getSubject());
        assertEquals("level-up-gamer", jwt.getClaimAsString("iss"));
        assertEquals(7L, ((Number) jwt.getClaims().get("usuarioId")).longValue());
        assertEquals("CLIENTE", jwt.getClaims().get("tipo"));
    }

    @Test
    void elTokenDuraTreintaMinutos() {
        Jwt jwt = decoder.decode(jwtService.generarToken(usuario));

        Duration duracion = Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt());

        assertEquals(Duration.ofMinutes(30), duracion);
    }

    @Test
    void unTokenModificadoSeRechaza() {
        String token = jwtService.generarToken(usuario);

        // Se cambia el último carácter de la firma
        char ultimo = token.charAt(token.length() - 1);
        String modificado = token.substring(0, token.length() - 1) + (ultimo == 'A' ? 'B' : 'A');

        assertThrows(JwtException.class, () -> decoder.decode(modificado));
    }

    @Test
    void unTokenFirmadoConOtraClaveSeRechaza() {
        SecretKey otraClave = new SecretKeySpec("otra-clave-de-prueba-32-bytes-01".getBytes(), "HmacSHA256");
        JwtService otroServicio = new JwtService(
                NimbusJwtEncoder.withSecretKey(otraClave).algorithm(MacAlgorithm.HS256).build());

        String tokenAjeno = otroServicio.generarToken(usuario);

        assertThrows(JwtException.class, () -> decoder.decode(tokenAjeno));
    }

    @Test
    void unTokenExpiradoSeRechaza() {
        Instant hace2Horas = Instant.now().minus(Duration.ofHours(2));

        JwtClaimsSet datos = JwtClaimsSet.builder()
                .issuer("level-up-gamer")
                .subject("cliente@duoc.cl")
                .issuedAt(hace2Horas)
                .expiresAt(hace2Horas.plus(Duration.ofMinutes(30)))
                .build();

        String expirado = encoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), datos)
        ).getTokenValue();

        assertThrows(JwtException.class, () -> decoder.decode(expirado));
    }

    @Test
    void unTokenDeOtroEmisorSeRechaza() {
        Instant ahora = Instant.now();

        JwtClaimsSet datos = JwtClaimsSet.builder()
                .issuer("otro-sistema")
                .subject("cliente@duoc.cl")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(Duration.ofMinutes(30)))
                .build();

        String ajeno = encoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), datos)
        ).getTokenValue();

        assertThrows(JwtException.class, () -> decoder.decode(ajeno));
    }
}
