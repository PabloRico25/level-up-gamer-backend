package cl.levelupgamer.backend.comun;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Comprueba que todos los errores salen con la forma común (CONTRATO_API.md, sección 6).
 * No arranca Spring completo ni necesita MySQL: usa un controlador de prueba que lanza cada excepción.
 */
class ManejadorErroresTests {

    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControladorDePrueba())
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    @Test
    @DisplayName("Un recurso que no existe responde 404 con la forma comun")
    void recursoNoEncontradoResponde404ConLaFormaComun() throws Exception {
        // Given: una ruta que lanza RecursoNoEncontradoException

        // When
        ResultActions respuesta = mockMvc.perform(get("/api/v1/prueba/no-encontrado"));

        // Then
        respuesta.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("No existe el producto JM999"))
                .andExpect(jsonPath("$.path").value("/api/v1/prueba/no-encontrado"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errores").doesNotExist());
    }

    @Test
    @DisplayName("Una solicitud invalida responde 400")
    void solicitudInvalidaResponde400() throws Exception {
        // Given: una ruta que lanza SolicitudInvalidaException

        // When
        ResultActions respuesta = mockMvc.perform(get("/api/v1/prueba/invalida"));

        // Then
        respuesta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("El precio debe ser mayor que 0"));
    }

    @Test
    @DisplayName("Un conflicto responde 409")
    void conflictoResponde409() throws Exception {
        // Given: una ruta que lanza ConflictoException

        // When
        ResultActions respuesta = mockMvc.perform(get("/api/v1/prueba/conflicto"));

        // Then
        respuesta.andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Ya existe un producto con el código JM001"));
    }

    @Test
    @DisplayName("Un error inesperado responde 500 sin mostrar detalles")
    void errorInesperadoResponde500SinRevelarDetalles() throws Exception {
        // Given: una ruta que lanza una excepcion que el sistema no preve

        // When
        ResultActions respuesta = mockMvc.perform(get("/api/v1/prueba/explota"));

        // Then
        respuesta.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Ocurrió un error interno"));
    }

    @Test
    @DisplayName("Un cuerpo que no valida responde 400 con la lista de campos")
    void validacionResponde400ConLaListaDeCampos() throws Exception {
        // Given: un cuerpo JSON con precio 0 (la regla pide al menos 1)

        // When
        ResultActions respuesta = mockMvc.perform(post("/api/v1/prueba/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"precio\": 0}"));

        // Then
        respuesta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errores[0].campo").value("precio"))
                .andExpect(jsonPath("$.errores[0].mensaje").value("Debe ser mayor que 0"));
    }

    @Test
    @DisplayName("Un metodo no permitido tambien usa la forma comun")
    void metodoNoPermitidoTambienUsaLaFormaComun() throws Exception {
        // Given: una ruta que solo acepta GET

        // When
        ResultActions respuesta = mockMvc.perform(post("/api/v1/prueba/no-encontrado"));

        // Then
        respuesta.andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.path").value("/api/v1/prueba/no-encontrado"));
    }

    /** Controlador solo para esta prueba: cada ruta lanza una excepción distinta. */
    @RestController
    static class ControladorDePrueba {

        @GetMapping("/api/v1/prueba/no-encontrado")
        String noEncontrado() {
            throw new RecursoNoEncontradoException("No existe el producto JM999");
        }

        @GetMapping("/api/v1/prueba/invalida")
        String invalida() {
            throw new SolicitudInvalidaException("El precio debe ser mayor que 0");
        }

        @GetMapping("/api/v1/prueba/conflicto")
        String conflicto() {
            throw new ConflictoException("Ya existe un producto con el código JM001");
        }

        @GetMapping("/api/v1/prueba/explota")
        String explota() {
            throw new IllegalStateException("detalle interno que no debe salir");
        }

        @PostMapping("/api/v1/prueba/validar")
        String validar(@Valid @RequestBody Datos datos) {
            return "ok";
        }
    }

    record Datos(@Min(value = 1, message = "Debe ser mayor que 0") long precio) {
    }
}