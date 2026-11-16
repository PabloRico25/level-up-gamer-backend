package cl.levelupgamer.backend.config;

import cl.levelupgamer.backend.auth.JwtService;
import cl.levelupgamer.backend.usuarios.RolUsuario;
import cl.levelupgamer.backend.usuarios.Usuario;
import cl.levelupgamer.backend.usuarios.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de seguridad contra la aplicación completa y MySQL: login, JWT y roles.
 * Se ejecutan con ./mvnw verify -DexcludedGroups=ninguno sobre una base de pruebas nueva.
 */
@org.junit.jupiter.api.Tag("integracion")
@SpringBootTest
@Transactional
class SeguridadRolesIntegracionTest {

    private static final String CLAVE = "Prueba12345";

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtEncoder jwtEncoder;

    private MockMvc mvc;

    private Usuario admin;
    private Usuario vendedor;
    private Usuario cliente;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();

        admin = crear("sec-admin@gmail.com", "10000013-K", RolUsuario.ADMINISTRADOR);
        vendedor = crear("sec-vendedor@gmail.com", "10000014-8", RolUsuario.VENDEDOR);
        cliente = crear("sec-cliente@duoc.cl", "10000015-6", RolUsuario.CLIENTE);
    }

    private Usuario crear(String correo, String run, RolUsuario rol) {
        Usuario u = new Usuario();
        u.setRun(run);
        u.setNombre("Prueba");
        u.setApellidos("Seguridad");
        u.setCorreo(correo);
        u.setContrasenaHash(passwordEncoder.encode(CLAVE));
        u.setFechaNacimiento(LocalDate.of(1995, 5, 5));
        u.setRegion("Metropolitana");
        u.setComuna("Santiago");
        u.setDireccion("Direccion de prueba");
        u.setTipo(rol);
        u.setActivo(true);
        return usuarioRepository.saveAndFlush(u);
    }

    private String bearer(Usuario u) {
        return "Bearer " + jwtService.generarToken(u);
    }

    // ---------------------------------------------------------------- login

    @Test
    void elLoginConDatosCorrectosEntregaUnTokenYElPerfil() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"sec-cliente@duoc.cl\",\"contrasena\":\"" + CLAVE + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.usuario.tipo").value("CLIENTE"));
    }

    @Test
    void elLoginConUnaClaveIncorrectaResponde401() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"sec-cliente@duoc.cl\",\"contrasena\":\"incorrecta1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void elLoginDeUnUsuarioDesactivadoResponde401() throws Exception {
        cliente.setActivo(false);
        usuarioRepository.saveAndFlush(cliente);

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"sec-cliente@duoc.cl\",\"contrasena\":\"" + CLAVE + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------------- sin token y token inválido

    @Test
    void sinTokenUnaRutaProtegidaResponde401() throws Exception {
        mvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void unTokenInventadoResponde401() throws Exception {
        mvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer esto.no.es-un-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unTokenExpiradoResponde401() throws Exception {
        Instant hace2Horas = Instant.now().minus(Duration.ofHours(2));

        JwtClaimsSet datos = JwtClaimsSet.builder()
                .issuer("level-up-gamer")
                .subject(admin.getCorreo())
                .issuedAt(hace2Horas)
                .expiresAt(hace2Horas.plus(Duration.ofMinutes(30)))
                .claim("usuarioId", admin.getId())
                .build();

        String expirado = jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), datos)
        ).getTokenValue();

        mvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + expirado))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------------- roles

    @Test
    void elAdministradorPuedeListarUsuarios() throws Exception {
        mvc.perform(get("/api/v1/usuarios").header("Authorization", bearer(admin)))
                .andExpect(status().isOk());
    }

    @Test
    void elClienteNoPuedeAdministrarUsuarios() throws Exception {
        mvc.perform(get("/api/v1/usuarios").header("Authorization", bearer(cliente)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void elVendedorNoPuedeAdministrarUsuarios() throws Exception {
        mvc.perform(get("/api/v1/usuarios").header("Authorization", bearer(vendedor)))
                .andExpect(status().isForbidden());
    }

    @Test
    void elVendedorNoPuedeCrearProductos() throws Exception {
        mvc.perform(post("/api/v1/productos")
                        .header("Authorization", bearer(vendedor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void elVendedorNoPuedeComprar() throws Exception {
        mvc.perform(post("/api/v1/boletas")
                        .header("Authorization", bearer(vendedor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void elVendedorPuedeConsultarLasOrdenes() throws Exception {
        mvc.perform(get("/api/v1/boletas").header("Authorization", bearer(vendedor)))
                .andExpect(status().isOk());
    }

    @Test
    void elCatalogoEsPublico() throws Exception {
        mvc.perform(get("/api/v1/productos")).andExpect(status().isOk());
    }

    // ---------------------------------------------------------------- el rol y el estado se leen en cada petición

    @Test
    void unCambioDeRolSurteEfectoConUnTokenYaEmitido() throws Exception {
        String token = bearer(admin);

        mvc.perform(get("/api/v1/usuarios").header("Authorization", token)).andExpect(status().isOk());

        admin.setTipo(RolUsuario.CLIENTE);
        usuarioRepository.saveAndFlush(admin);

        mvc.perform(get("/api/v1/usuarios").header("Authorization", token)).andExpect(status().isForbidden());
    }

    @Test
    void unUsuarioDesactivadoPierdeElAccesoConSuTokenYaEmitido() throws Exception {
        String token = bearer(admin);

        mvc.perform(get("/api/v1/usuarios").header("Authorization", token)).andExpect(status().isOk());

        admin.setActivo(false);
        usuarioRepository.saveAndFlush(admin);

        mvc.perform(get("/api/v1/usuarios").header("Authorization", token)).andExpect(status().isUnauthorized());
    }
}
