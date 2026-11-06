package cl.levelupgamer.backend.productos;

import cl.levelupgamer.backend.categorias.Categoria;
import cl.levelupgamer.backend.categorias.CategoriaRepository;
import cl.levelupgamer.backend.comun.ConflictoException;
import cl.levelupgamer.backend.comun.RecursoNoEncontradoException;
import cl.levelupgamer.backend.comun.SolicitudInvalidaException;
import cl.levelupgamer.backend.productos.dto.ProductoRequest;
import cl.levelupgamer.backend.productos.dto.ProductoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTests {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    private ProductoService servicio;
    private Categoria juegosDeMesa;

    @BeforeEach
    void preparar() {
        servicio = new ProductoService(productoRepository, categoriaRepository);
        juegosDeMesa = new Categoria();
        juegosDeMesa.setId(1L);
        juegosDeMesa.setNombre("Juegos de Mesa");
    }

    private ProductoRequest solicitud(String id) {
        return new ProductoRequest(id, "  Catan ", 1L, 29990L, 12, 3, " CATAN Studio ", "/assets/img/catan.jpg",
                "Juego de estrategia");
    }

    private Producto productoExistente() {
        Producto producto = new Producto();
        producto.setId("JM001");
        producto.setNombre("Catan");
        producto.setCategoria(juegosDeMesa);
        producto.setPrecio(29990L);
        producto.setStock(12);
        producto.setFabricante("CATAN Studio");
        return producto;
    }

    @Test
    @DisplayName("Crear guarda el producto y devuelve categoriaId y nombre de la categoria")
    void crearGuardaElProductoYDevuelveCategoriaIdYNombre() {
        // Given: codigo libre y categoria existente
        when(productoRepository.existsById("JM001")).thenReturn(false);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(juegosDeMesa));
        when(productoRepository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        // When
        ProductoResponse respuesta = servicio.crear(solicitud("JM001"));

        // Then
        assertEquals("JM001", respuesta.id());
        assertEquals("Catan", respuesta.nombre());
        assertEquals(1L, respuesta.categoriaId());
        assertEquals("Juegos de Mesa", respuesta.categoria());
        assertEquals(29990L, respuesta.precio());
        assertEquals(3, respuesta.stockCritico());
        assertEquals("CATAN Studio", respuesta.fabricante());
    }

    @Test
    @DisplayName("Crear con un codigo repetido lanza conflicto")
    void crearConCodigoRepetidoLanzaConflicto() {
        // Given: el codigo ya existe
        when(productoRepository.existsById("JM001")).thenReturn(true);

        // When
        ConflictoException error = assertThrows(ConflictoException.class, () -> servicio.crear(solicitud("JM001")));

        // Then
        assertEquals("Ya existe un producto con el código JM001", error.getMessage());
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear sin codigo lanza solicitud invalida")
    void crearSinCodigoLanzaSolicitudInvalida() {
        // When y Then: codigo nulo y codigo en blanco
        assertThrows(SolicitudInvalidaException.class, () -> servicio.crear(solicitud(null)));
        assertThrows(SolicitudInvalidaException.class, () -> servicio.crear(solicitud("  ")));
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear con una categoria inexistente lanza solicitud invalida")
    void crearConCategoriaInexistenteLanzaSolicitudInvalida() {
        // Given: la categoria no existe
        when(productoRepository.existsById("JM001")).thenReturn(false);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        // When y Then
        assertThrows(SolicitudInvalidaException.class, () -> servicio.crear(solicitud("JM001")));
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("El stock critico puede quedar nulo")
    void stockCriticoPuedeQuedarNulo() {
        // Given: una solicitud sin stock critico
        when(productoRepository.existsById("JM001")).thenReturn(false);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(juegosDeMesa));
        when(productoRepository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));
        ProductoRequest sinCritico = new ProductoRequest("JM001", "Catan", 1L, 29990L, 12, null, "CATAN Studio",
                null, null);

        // When y Then
        assertNull(servicio.crear(sinCritico).stockCritico());
    }

    @Test
    @DisplayName("Actualizar cambia los datos y usa el codigo de la ruta")
    void actualizarCambiaLosDatosYUsaElCodigoDeLaRuta() {
        // Given: un producto activo y un cuerpo con otro codigo
        Producto existente = productoExistente();
        when(productoRepository.buscarActivoPorId("JM001")).thenReturn(Optional.of(existente));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(juegosDeMesa));
        ProductoRequest cambios = new ProductoRequest("OTRO99", "Catan Edición 2", 1L, 34990L, 8, null,
                "CATAN Studio", null, null);

        // When
        ProductoResponse respuesta = servicio.actualizar("JM001", cambios);

        // Then: manda el codigo de la ruta
        assertEquals("JM001", respuesta.id());
        assertEquals("Catan Edición 2", respuesta.nombre());
        assertEquals(34990L, respuesta.precio());
        assertEquals(8, respuesta.stock());
    }

    @Test
    @DisplayName("Actualizar un producto inexistente lanza 404")
    void actualizarUnProductoInexistenteLanza404() {
        // Given
        when(productoRepository.buscarActivoPorId("JM999")).thenReturn(Optional.empty());

        // When y Then
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.actualizar("JM999", solicitud("JM999")));
    }

    @Test
    @DisplayName("Buscar un producto inexistente o inactivo lanza 404")
    void buscarPorIdDeUnProductoInexistenteOInactivoLanza404() {
        // Given
        when(productoRepository.buscarActivoPorId("JM999")).thenReturn(Optional.empty());

        // When
        RecursoNoEncontradoException error = assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.buscarPorId("JM999"));

        // Then
        assertEquals("No existe el producto JM999", error.getMessage());
    }

    @Test
    @DisplayName("Desactivar marca activo en falso sin borrar")
    void desactivarMarcaActivoEnFalsoSinBorrar() {
        // Given
        Producto existente = productoExistente();
        when(productoRepository.buscarActivoPorId("JM001")).thenReturn(Optional.of(existente));

        // When
        servicio.desactivar("JM001");

        // Then
        assertFalse(existente.isActivo());
        verify(productoRepository, never()).delete(any());
        verify(productoRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Listar devuelve solo lo que entrega el repositorio de activos")
    void listarDevuelveSoloLoQueEntregaElRepositorioDeActivos() {
        // Given
        when(productoRepository.listarActivos()).thenReturn(List.of(productoExistente()));

        // When
        List<ProductoResponse> lista = servicio.listar();

        // Then
        assertEquals(1, lista.size());
        assertEquals("JM001", lista.get(0).id());
    }

    // ---- Contrato del stock con las compras ----

    @Test
    @DisplayName("Descontar stock llama una sola vez a la sentencia atomica")
    void descontarStockLlamaUnaSolaVezALaSentenciaAtomica() {
        // Given: el repositorio descuenta una fila
        when(productoRepository.descontarStock("JM001", 3)).thenReturn(1);

        // When
        servicio.descontarStock("JM001", 3);

        // Then
        verify(productoRepository).descontarStock("JM001", 3);
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Descontar sin stock suficiente lanza conflicto con lo que queda")
    void descontarStockSinStockSuficienteLanzaConflictoConLoQueQueda() {
        // Given: el producto tiene 12 y se piden 20
        Producto existente = productoExistente();
        when(productoRepository.descontarStock("JM001", 20)).thenReturn(0);
        when(productoRepository.buscarActivoPorId("JM001")).thenReturn(Optional.of(existente));

        // When
        ConflictoException error = assertThrows(ConflictoException.class, () -> servicio.descontarStock("JM001", 20));

        // Then
        assertEquals("Stock insuficiente para Catan: quedan 12 y se pidieron 20", error.getMessage());
    }

    @Test
    @DisplayName("Descontar stock de un producto inexistente lanza 404")
    void descontarStockDeUnProductoInexistenteLanza404() {
        // Given: ninguna fila cambia y el producto no existe
        when(productoRepository.descontarStock("JM999", 1)).thenReturn(0);
        when(productoRepository.buscarActivoPorId("JM999")).thenReturn(Optional.empty());

        // When y Then
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.descontarStock("JM999", 1));
    }

    @Test
    @DisplayName("Descontar o reponer una cantidad no positiva lanza solicitud invalida")
    void descontarOReponerUnaCantidadNoPositivaLanzaSolicitudInvalida() {
        // When y Then
        assertThrows(SolicitudInvalidaException.class, () -> servicio.descontarStock("JM001", 0));
        assertThrows(SolicitudInvalidaException.class, () -> servicio.descontarStock("JM001", -2));
        assertThrows(SolicitudInvalidaException.class, () -> servicio.reponerStock("JM001", 0));
        verify(productoRepository, never()).descontarStock(any(), anyInt());
        verify(productoRepository, never()).reponerStock(any(), anyInt());
    }

    @Test
    @DisplayName("Reponer suma stock y un producto inexistente lanza 404")
    void reponerStockSumaYUnProductoInexistenteLanza404() {
        // Given
        when(productoRepository.reponerStock("JM001", 2)).thenReturn(1);
        when(productoRepository.reponerStock("JM999", 2)).thenReturn(0);

        // When
        servicio.reponerStock("JM001", 2);

        // Then
        verify(productoRepository).reponerStock("JM001", 2);
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.reponerStock("JM999", 2));
    }
}