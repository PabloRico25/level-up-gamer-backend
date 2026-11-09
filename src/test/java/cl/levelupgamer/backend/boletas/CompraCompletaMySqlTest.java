
package cl.levelupgamer.backend.boletas;

import cl.levelupgamer.backend.boletas.dto.BoletaResponse;
import cl.levelupgamer.backend.boletas.dto.CrearBoletaRequest;
import cl.levelupgamer.backend.boletas.dto.ItemCompraRequest;
import cl.levelupgamer.backend.usuarios.RolUsuario;
import cl.levelupgamer.backend.usuarios.Usuario;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Import(CompraCompletaMySqlTest.Config.class)
class CompraCompletaMySqlTest {

    @Autowired
    private RegistrarCompraService registrarCompraService;

    @Autowired
    private JdbcTemplate jdbc;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void compraRealConDescuentoYStock() {

        // Stock original del producto
        Integer stockAntes = jdbc.queryForObject(
                "SELECT stock FROM producto WHERE id = ?",
                Integer.class,
                "JM001"
        );

        assertNotNull(stockAntes);
        assertTrue(stockAntes >= 2);

        // Usuario temporal
        Usuario usuario = new Usuario();
        usuario.setRun("18888888-8");
        usuario.setNombre("Gabo");
        usuario.setApellidos("Prueba");
        usuario.setCorreo("gabo-prueba@duoc.cl");
        usuario.setContrasenaHash("solo-prueba");
        usuario.setFechaNacimiento(LocalDate.of(2000, 1, 1));
        usuario.setRegion("Metropolitana");
        usuario.setComuna("Santiago");
        usuario.setDireccion("Direccion de prueba");
        usuario.setTipo(RolUsuario.CLIENTE);

        entityManager.persist(usuario);
        entityManager.flush();

        // Comprar dos unidades de Catan
        ItemCompraRequest item = new ItemCompraRequest();
        item.setProductoId("JM001");
        item.setCantidad(2);

        CrearBoletaRequest solicitud = new CrearBoletaRequest();
        solicitud.setItems(List.of(item));

        // Ejecutar nuestro servicio real de compras
        BoletaResponse respuesta =
                registrarCompraService.registrar(solicitud, usuario);

        entityManager.flush();

        // Comprobar los montos
        assertEquals(59980L, respuesta.getSubtotal());
        assertEquals(11996L, respuesta.getDescuento());
        assertEquals(47984L, respuesta.getTotal());

        // Comprobar stock real
        Integer stockDespues = jdbc.queryForObject(
                "SELECT stock FROM producto WHERE id = ?",
                Integer.class,
                "JM001"
        );

        assertNotNull(stockDespues);
        assertEquals(stockAntes - 2, stockDespues);

        // Comprobar boleta guardada en MySQL
        Long totalGuardado = jdbc.queryForObject(
                "SELECT total FROM boleta WHERE id = ?",
                Long.class,
                respuesta.getId()
        );

        assertEquals(47984L, totalGuardado);

        // Comprobar detalle guardado en MySQL
        Long cantidadDetalles = jdbc.queryForObject(
                "SELECT COUNT(*) FROM detalle_boleta WHERE boleta_id = ?",
                Long.class,
                respuesta.getId()
        );

        assertEquals(1L, cantidadDetalles);
    }

    // Inventario temporal: SOLO para esta prueba
    @TestConfiguration
    static class Config {

        @Bean
        InventarioCompra inventarioCompraParaPrueba(
                JdbcTemplate jdbc) {

            return new InventarioCompra() {

                @Override
                public CotizadorCompra.ProductoDisponible buscarProducto(
                        String productoId) {

                    try {
                        return jdbc.queryForObject(
                                "SELECT precio, stock, activo " +
                                        "FROM producto WHERE id = ?",
                                (rs, fila) ->
                                        new CotizadorCompra.ProductoDisponible(
                                                rs.getLong("precio"),
                                                rs.getInt("stock"),
                                                rs.getBoolean("activo")
                                        ),
                                productoId
                        );

                    } catch (EmptyResultDataAccessException error) {
                        throw new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Producto no encontrado",
                                error
                        );
                    }
                }

                @Override
                public void descontarStock(
                        String productoId,
                        int cantidad) {

                    int filas = jdbc.update(
                            "UPDATE producto " +
                                    "SET stock = stock - ? " +
                                    "WHERE id = ? " +
                                    "AND activo = TRUE " +
                                    "AND stock >= ?",
                            cantidad,
                            productoId,
                            cantidad
                    );

                    if (filas != 1) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Stock insuficiente"
                        );
                    }
                }
            };
        }
    }
}
