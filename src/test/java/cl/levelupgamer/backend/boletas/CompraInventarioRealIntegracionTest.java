package cl.levelupgamer.backend.boletas;
import cl.levelupgamer.backend.boletas.dto.*;
import cl.levelupgamer.backend.usuarios.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
@Tag("integracion") @SpringBootTest
class CompraInventarioRealIntegracionTest {
 @Autowired RegistrarCompraService compras;
 @Autowired JdbcTemplate jdbc;
 @Autowired UsuarioRepository usuarios;
 @BeforeEach void preparar() {
  jdbc.update("INSERT INTO producto(id,nombre,categoria_id,precio,stock,fabricante,activo) VALUES ('TEST-REAL','Prueba real',1,100,5,'Prueba',true)");
 }
 @AfterEach void limpiar() {jdbc.update("DELETE FROM producto WHERE id='TEST-REAL'");}
 @Test void fallaAlGuardarBoletaRevierteDescuentoReal() {
  var item=new ItemCompraRequest();item.setProductoId("TEST-REAL");item.setCantidad(2);
  var solicitud=new CrearBoletaRequest();solicitud.setItems(List.of(item));
  // FK inexistente provoca fallo DESPUÉS del descuento, usando servicios reales.
  var usuario=new Usuario();usuario.setId(-99999L);usuario.setCorreo("prueba@gmail.com");
  long antes=jdbc.queryForObject("SELECT COUNT(*) FROM boleta",Long.class);
  assertThrows(RuntimeException.class,()->compras.registrar(solicitud,usuario));
  assertEquals(5,jdbc.queryForObject("SELECT stock FROM producto WHERE id='TEST-REAL'",Integer.class));
  assertEquals(antes,jdbc.queryForObject("SELECT COUNT(*) FROM boleta",Long.class));
 }
}
