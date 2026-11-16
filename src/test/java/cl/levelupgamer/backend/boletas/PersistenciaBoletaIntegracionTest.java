
package cl.levelupgamer.backend.boletas;

import cl.levelupgamer.backend.boletas.dto.BoletaResponse;
import cl.levelupgamer.backend.boletas.dto.DetalleBoletaResponse;
import cl.levelupgamer.backend.usuarios.Usuario;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@org.junit.jupiter.api.Tag("integracion")
@SpringBootTest
@Transactional
class PersistenciaBoletaIntegracionTest {

    @Autowired
    private PersistenciaBoletaService persistenciaBoletaService;

    @Autowired
    private BoletaRepository boletaRepository;

    @Autowired
    private DetalleBoletaRepository detalleBoletaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void guardarBoletaEnMySQL() {

        // Crear un usuario temporal para la prueba
        Usuario usuario = new Usuario();
        usuario.setRun("19999999-K");
        usuario.setNombre("Usuario");
        usuario.setApellidos("Prueba");
        usuario.setCorreo("prueba-compra-gabo@ejemplo.com");
        usuario.setContrasenaHash("solo-para-prueba");
        usuario.setFechaNacimiento(LocalDate.of(2000, 1, 1));
        usuario.setRegion("Metropolitana");
        usuario.setComuna("Santiago");
        usuario.setDireccion("Direccion de prueba");

        entityManager.persist(usuario);
        entityManager.flush();

        // Preparar una compra de un producto real del catálogo
        DetalleBoletaResponse detalle =
                new DetalleBoletaResponse(
                        "JM001",
                        1,
                        29990L,
                        29990L
                );

        CotizadorCompra.ResultadoCompra resultado =
                new CotizadorCompra.ResultadoCompra(
                        29990L,
                        0L,
                        29990L,
                        List.of(detalle)
                );

        // Guardar la boleta usando nuestro servicio real
        BoletaResponse respuesta =
                persistenciaBoletaService.guardar(
                        usuario,
                        resultado
                );

        // Enviar los cambios a MySQL para comprobarlos
        entityManager.flush();
        entityManager.clear();

        // Consultar lo que quedó guardado
        Boleta boleta = boletaRepository
                .findById(respuesta.getId())
                .orElseThrow();

        List<DetalleBoleta> detalles =
                detalleBoletaRepository.findByBoletaId(
                        respuesta.getId()
                );

        // Comprobar los resultados
        assertNotNull(boleta.getId());
        assertEquals(29990L, boleta.getTotal());
        assertEquals(1, detalles.size());
        assertEquals("JM001", detalles.get(0).getProductoId());
        assertEquals(1, detalles.get(0).getCantidad());
    }
}
