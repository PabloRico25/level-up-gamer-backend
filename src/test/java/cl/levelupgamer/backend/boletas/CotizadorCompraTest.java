
package cl.levelupgamer.backend.boletas;

import cl.levelupgamer.backend.boletas.dto.CrearBoletaRequest;
import cl.levelupgamer.backend.boletas.dto.ItemCompraRequest;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CotizadorCompraTest {

    private final CotizadorCompra cotizador = new CotizadorCompra(
            new ValidadorCompra(),
            new CalculadoraCompra(),
            new DescuentoDuoc()
    );

    private ItemCompraRequest crearItem(String id, int cantidad) {
        ItemCompraRequest item = new ItemCompraRequest();
        item.setProductoId(id);
        item.setCantidad(cantidad);
        return item;
    }

    private CrearBoletaRequest crearSolicitud(
            List<ItemCompraRequest> items) {

        CrearBoletaRequest solicitud = new CrearBoletaRequest();
        solicitud.setItems(items);
        return solicitud;
    }

    @Test
    void cotizarCompraConDescuentoDuoc() {

        CrearBoletaRequest solicitud = crearSolicitud(
                List.of(
                        crearItem("JM001", 2),
                        crearItem("AC001", 1)
                )
        );

        Map<String, CotizadorCompra.ProductoDisponible> productos =
                Map.of(
                        "JM001", new CotizadorCompra.ProductoDisponible(
                                25000L, 10, true
                        ),
                        "AC001", new CotizadorCompra.ProductoDisponible(
                                15000L, 5, true
                        )
                );

        CotizadorCompra.ResultadoCompra resultado =
                cotizador.cotizar(
                        solicitud,
                        "gabo@duoc.cl",
                        productos
                );

        assertEquals(65000L, resultado.subtotal());
        assertEquals(13000L, resultado.descuento());
        assertEquals(52000L, resultado.total());
        assertEquals(2, resultado.items().size());
    }

    @Test
    void cotizarCompraSinDescuento() {

        CrearBoletaRequest solicitud = crearSolicitud(
                List.of(crearItem("JM001", 1))
        );

        var productos = Map.of(
                "JM001",
                new CotizadorCompra.ProductoDisponible(
                        25000L, 10, true
                )
        );

        var resultado = cotizador.cotizar(
                solicitud,
                "gabo@gmail.com",
                productos
        );

        assertEquals(25000L, resultado.total());
        assertEquals(0L, resultado.descuento());
    }

    @Test
    void rechazarCompraConStockInsuficiente() {

        CrearBoletaRequest solicitud = crearSolicitud(
                List.of(crearItem("JM001", 5))
        );

        var productos = Map.of(
                "JM001",
                new CotizadorCompra.ProductoDisponible(
                        25000L, 2, true
                )
        );

        assertThrows(
                ResponseStatusException.class,
                () -> cotizador.cotizar(
                        solicitud,
                        "gabo@duoc.cl",
                        productos
                )
        );
    }

    @Test
    void rechazarProductoInactivo() {

        CrearBoletaRequest solicitud = crearSolicitud(
                List.of(crearItem("JM001", 1))
        );

        var productos = Map.of(
                "JM001",
                new CotizadorCompra.ProductoDisponible(
                        25000L, 10, false
                )
        );

        assertThrows(
                ResponseStatusException.class,
                () -> cotizador.cotizar(
                        solicitud,
                        "gabo@gmail.com",
                        productos
                )
        );
    }
}
