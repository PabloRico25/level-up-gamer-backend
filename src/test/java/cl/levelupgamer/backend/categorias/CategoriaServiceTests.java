package cl.levelupgamer.backend.categorias;

import cl.levelupgamer.backend.categorias.dto.CategoriaRequest;
import cl.levelupgamer.backend.categorias.dto.CategoriaResponse;
import cl.levelupgamer.backend.comun.ConflictoException;
import cl.levelupgamer.backend.comun.RecursoNoEncontradoException;
import cl.levelupgamer.backend.productos.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTests {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProductoRepository productoRepository;

    private CategoriaService servicio;
    private Categoria mouse;

    @BeforeEach
    void preparar() {
        servicio = new CategoriaService(categoriaRepository, productoRepository);
        mouse = new Categoria();
        mouse.setId(6L);
        mouse.setNombre("Mouse");
    }

    @Test
    @DisplayName("Crear guarda la categoria sin espacios sobrantes")
    void crearGuardaLaCategoriaSinEspaciosSobrantes() {
        // Given: un nombre que no existe y que llega con espacios
        when(categoriaRepository.existsByNombreIgnoreCase("Teclados")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(i -> {
            Categoria c = i.getArgument(0);
            c.setId(9L);
            return c;
        });

        // When
        CategoriaResponse respuesta = servicio.crear(new CategoriaRequest("  Teclados "));

        // Then
        assertEquals(9L, respuesta.id());
        assertEquals("Teclados", respuesta.nombre());
    }

    @Test
    @DisplayName("Crear con un nombre repetido lanza conflicto")
    void crearConNombreRepetidoLanzaConflicto() {
        // Given: ya existe una categoria con ese nombre
        when(categoriaRepository.existsByNombreIgnoreCase("Mouse")).thenReturn(true);

        // When y Then
        assertThrows(ConflictoException.class, () -> servicio.crear(new CategoriaRequest("Mouse")));
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Buscar una categoria inexistente lanza 404")
    void buscarUnaCategoriaInexistenteLanza404() {
        // Given: el id no existe
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        // When y Then
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.buscarPorId(99L));
    }

    @Test
    @DisplayName("Actualizar cambia el nombre")
    void actualizarCambiaElNombre() {
        // Given: la categoria existe y el nombre nuevo esta libre
        when(categoriaRepository.findById(6L)).thenReturn(Optional.of(mouse));
        when(categoriaRepository.existsByNombreIgnoreCaseAndIdNot("Ratones", 6L)).thenReturn(false);

        // When
        CategoriaResponse respuesta = servicio.actualizar(6L, new CategoriaRequest("Ratones"));

        // Then
        assertEquals("Ratones", respuesta.nombre());
    }

    @Test
    @DisplayName("Actualizar con el nombre de otra categoria lanza conflicto")
    void actualizarConElNombreDeOtraCategoriaLanzaConflicto() {
        // Given: otro registro ya usa ese nombre
        when(categoriaRepository.findById(6L)).thenReturn(Optional.of(mouse));
        when(categoriaRepository.existsByNombreIgnoreCaseAndIdNot("Consolas", 6L)).thenReturn(true);

        // When y Then
        assertThrows(ConflictoException.class, () -> servicio.actualizar(6L, new CategoriaRequest("Consolas")));
    }

    @Test
    @DisplayName("Eliminar una categoria con productos lanza conflicto")
    void eliminarUnaCategoriaConProductosLanzaConflicto() {
        // Given: la categoria tiene productos
        when(categoriaRepository.findById(6L)).thenReturn(Optional.of(mouse));
        when(productoRepository.existsByCategoriaId(6L)).thenReturn(true);

        // When y Then
        assertThrows(ConflictoException.class, () -> servicio.eliminar(6L));
        verify(categoriaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Eliminar una categoria sin productos la borra")
    void eliminarUnaCategoriaSinProductosLaBorra() {
        // Given: la categoria no tiene productos
        when(categoriaRepository.findById(6L)).thenReturn(Optional.of(mouse));
        when(productoRepository.existsByCategoriaId(6L)).thenReturn(false);

        // When
        servicio.eliminar(6L);

        // Then
        verify(categoriaRepository).delete(mouse);
    }
}