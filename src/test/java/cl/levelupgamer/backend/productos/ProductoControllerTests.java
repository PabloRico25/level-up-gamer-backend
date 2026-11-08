package cl.levelupgamer.backend.productos;

import cl.levelupgamer.backend.comun.ConflictoException;
import cl.levelupgamer.backend.comun.ManejadorErrores;
import cl.levelupgamer.backend.comun.RecursoNoEncontradoException;
import cl.levelupgamer.backend.productos.dto.ProductoRequest;
import cl.levelupgamer.backend.productos.dto.ProductoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Rutas, códigos HTTP, forma del JSON y validaciones del controlador, sin base de datos (el servicio es un mock). */
@ExtendWith(MockitoExtension.class)
class ProductoControllerTests {

    @Mock
    private ProductoService productoService;

    private MockMvc mockMvc;

    private static final ProductoResponse CATAN = new ProductoResponse("JM001", "Catan", 1L, "Juegos de Mesa", 29990L,
            12, 3, "CATAN Studio", "/assets/img/catan.jpg", "Juego de estrategia");

    private static final String CUERPO_VALIDO = """
            {"id":"JM001","nombre":"Catan","categoriaId":1,"precio":29990,"stock":12,"stockCritico":3,
             "fabricante":"CATAN Studio","imagen":"/assets/img/catan.jpg","descripcion":"Juego de estrategia"}""";

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProductoController(productoService))
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    @Test
    @DisplayName("Listar devuelve los campos del contrato")
    void listarDevuelveLosCamposDelContrato() throws Exception {
        // Given: el servicio entrega un producto
        when(productoService.listar()).thenReturn(List.of(CATAN));

        // When
        ResultActions respuesta = mockMvc.perform(get("/api/v1/productos"));

        // Then
        respuesta.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("JM001"))
                .andExpect(jsonPath("$[0].categoriaId").value(1))
                .andExpect(jsonPath("$[0].categoria").value("Juegos de Mesa"))
                .andExpect(jsonPath("$[0].precio").value(29990))
                .andExpect(jsonPath("$[0].stockCritico").value(3));
    }

    @Test
    @DisplayName("El detalle de un producto inexistente responde 404")
    void detalleDeUnProductoInexistenteResponde404() throws Exception {
        // Given
        when(productoService.buscarPorId("JM999")).thenThrow(new RecursoNoEncontradoException("No existe el producto JM999"));

        // When
        ResultActions respuesta = mockMvc.perform(get("/api/v1/productos/JM999"));

        // Then
        respuesta.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe el producto JM999"))
                .andExpect(jsonPath("$.path").value("/api/v1/productos/JM999"));
    }

    @Test
    @DisplayName("Crear responde 201 con la cabecera Location")
    void crearResponde201ConLocation() throws Exception {
        // Given
        when(productoService.crear(any(ProductoRequest.class))).thenReturn(CATAN);

        // When
        ResultActions respuesta = mockMvc.perform(
                post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO));

        // Then
        respuesta.andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/productos/JM001"))
                .andExpect(jsonPath("$.id").value("JM001"));
    }

    @Test
    @DisplayName("Crear con un codigo repetido responde 409")
    void crearConCodigoRepetidoResponde409() throws Exception {
        // Given
        when(productoService.crear(any(ProductoRequest.class)))
                .thenThrow(new ConflictoException("Ya existe un producto con el código JM001"));

        // When
        ResultActions respuesta = mockMvc.perform(
                post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO));

        // Then
        respuesta.andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Crear con precio negativo responde 400 y no llama al servicio")
    void crearConPrecioNegativoResponde400YNoLlamaAlServicio() throws Exception {
        // Given: un cuerpo con precio -5
        String cuerpo = CUERPO_VALIDO.replace("29990", "-5");

        // When
        ResultActions respuesta = mockMvc.perform(
                post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content(cuerpo));

        // Then
        respuesta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[0].campo").value("precio"))
                .andExpect(jsonPath("$.errores[0].mensaje").value("Debe ser mayor que 0"));
        verify(productoService, never()).crear(any());
    }

    @Test
    @DisplayName("Crear con stock negativo responde 400")
    void crearConStockNegativoResponde400() throws Exception {
        // Given
        String cuerpo = CUERPO_VALIDO.replace("\"stock\":12", "\"stock\":-1");

        // When
        ResultActions respuesta = mockMvc.perform(
                post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content(cuerpo));

        // Then
        respuesta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[0].campo").value("stock"));
    }

    @Test
    @DisplayName("Crear con stock critico negativo responde 400")
    void crearConStockCriticoNegativoResponde400() throws Exception {
        // Given
        String cuerpo = CUERPO_VALIDO.replace("\"stockCritico\":3", "\"stockCritico\":-2");

        // When
        ResultActions respuesta = mockMvc.perform(
                post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content(cuerpo));

        // Then
        respuesta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[0].campo").value("stockCritico"));
    }

    @Test
    @DisplayName("Crear sin campos obligatorios lista cada uno")
    void crearSinCamposObligatoriosListaCadaUno() throws Exception {
        // When: un cuerpo vacio
        ResultActions respuesta = mockMvc.perform(
                post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content("{}"));

        // Then: nombre, categoria, precio, stock y fabricante
        respuesta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.length()").value(5));
    }

    @Test
    @DisplayName("Crear con un codigo de formato invalido responde 400")
    void crearConCodigoInvalidoResponde400() throws Exception {
        // Given: un codigo con minusculas y espacio
        String cuerpo = CUERPO_VALIDO.replace("\"id\":\"JM001\"", "\"id\":\"jm 1\"");

        // When
        ResultActions respuesta = mockMvc.perform(
                post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content(cuerpo));

        // Then
        respuesta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[0].campo").value("id"));
    }

    @Test
    @DisplayName("Editar responde 200")
    void editarResponde200() throws Exception {
        // Given
        when(productoService.actualizar(eq("JM001"), any(ProductoRequest.class))).thenReturn(CATAN);

        // When
        ResultActions respuesta = mockMvc.perform(
                put("/api/v1/productos/JM001").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO));

        // Then
        respuesta.andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Catan"));
    }

    @Test
    @DisplayName("Desactivar responde 204")
    void desactivarResponde204() throws Exception {
        // When
        ResultActions respuesta = mockMvc.perform(delete("/api/v1/productos/JM001"));

        // Then
        respuesta.andExpect(status().isNoContent());
        verify(productoService).desactivar("JM001");
    }
}