package cl.levelupgamer.backend.boletas;

import cl.levelupgamer.backend.boletas.dto.CrearBoletaRequest;
import cl.levelupgamer.backend.boletas.dto.ItemCompraRequest;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidadorCompraTest {

    private final ValidadorCompra validador = new ValidadorCompra();

    private ItemCompraRequest crearItem(String productoId, Integer cantidad) {
        ItemCompraRequest item = new ItemCompraRequest();
        item.setProductoId(productoId);
        item.setCantidad(cantidad);
        return item;
    }

    private CrearBoletaRequest crearSolicitud(List<ItemCompraRequest> items) {
        CrearBoletaRequest solicitud = new CrearBoletaRequest();
        solicitud.setItems(items);
        return solicitud;
    }

    @Test
    void aceptarCompraValida() {
        CrearBoletaRequest solicitud = crearSolicitud(
                List.of(crearItem("JM001", 2))
        );

        assertDoesNotThrow(() -> validador.validar(solicitud));
    }

    @Test
    void rechazarCompraVacia() {
        CrearBoletaRequest solicitud = crearSolicitud(List.of());

        assertThrows(ResponseStatusException.class,
                () -> validador.validar(solicitud));
    }

    @Test
    void rechazarCantidadCero() {
        CrearBoletaRequest solicitud = crearSolicitud(
                List.of(crearItem("JM001", 0))
        );

        assertThrows(ResponseStatusException.class,
                () -> validador.validar(solicitud));
    }

    @Test
    void rechazarProductoSinCodigo() {
        CrearBoletaRequest solicitud = crearSolicitud(
                List.of(crearItem("", 2))
        );

        assertThrows(ResponseStatusException.class,
                () -> validador.validar(solicitud));
    }

    @Test
    void rechazarProductosDuplicados() {
        CrearBoletaRequest solicitud = crearSolicitud(
                List.of(
                        crearItem("JM001", 2),
                        crearItem("JM001", 1)
                )
        );

        assertThrows(ResponseStatusException.class,
                () -> validador.validar(solicitud));
    }
}