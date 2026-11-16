
package cl.levelupgamer.backend.boletas;

import cl.levelupgamer.backend.boletas.dto.BoletaResponse;
import cl.levelupgamer.backend.boletas.dto.CrearBoletaRequest;
import cl.levelupgamer.backend.boletas.dto.ItemCompraRequest;
import cl.levelupgamer.backend.usuarios.Usuario;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RegistrarCompraServiceTest {

    @Test
    void registrarCompraConDescuentoYStock() {

        // Simular el inventario y el guardado de boletas
        InventarioCompra inventario = mock(InventarioCompra.class);
        PersistenciaBoletaService persistencia =
                mock(PersistenciaBoletaService.class);

        @SuppressWarnings("unchecked")
        ObjectProvider<InventarioCompra> proveedor =
                mock(ObjectProvider.class);

        when(proveedor.getIfAvailable()).thenReturn(inventario);

        // Simular un teclado de $25.000 con 10 unidades
        when(inventario.buscarProducto("JM001"))
                .thenReturn(
                        new CotizadorCompra.ProductoDisponible(
                                25000L, 10, true
                        )
                );

        CotizadorCompra cotizador = new CotizadorCompra(
                new ValidadorCompra(),
                new CalculadoraCompra(),
                new DescuentoDuoc()
        );

        RegistrarCompraService servicio = new RegistrarCompraService(
                new ValidadorCompra(),
                cotizador,
                persistencia,
                proveedor
        );

        // Crear un usuario Duoc de prueba
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setCorreo("gabo@duoc.cl");

        // Preparar una compra de dos teclados
        ItemCompraRequest item = new ItemCompraRequest();
        item.setProductoId("JM001");
        item.setCantidad(2);

        CrearBoletaRequest solicitud = new CrearBoletaRequest();
        solicitud.setItems(List.of(item));

        // Simular la respuesta del guardado
        when(persistencia.guardar(
                eq(usuario),
                any(CotizadorCompra.ResultadoCompra.class)
        )).thenAnswer(invocacion -> {

            CotizadorCompra.ResultadoCompra resultado =
                    invocacion.getArgument(1);

            return new BoletaResponse(
                    1L,
                    LocalDateTime.now(),
                    resultado.subtotal(),
                    resultado.descuento(),
                    resultado.total(),
                    resultado.items()
            );
        });

        // Ejecutar la compra
        BoletaResponse respuesta =
                servicio.registrar(solicitud, usuario);

        // Comprobar los cálculos
        assertEquals(50000L, respuesta.getSubtotal());
        assertEquals(10000L, respuesta.getDescuento());
        assertEquals(40000L, respuesta.getTotal());

        // Comprobar que se solicitó descontar stock
        verify(inventario).descontarStock("JM001", 2);

        // Comprobar que se solicitó guardar la boleta
        verify(persistencia).guardar(
                eq(usuario),
                any(CotizadorCompra.ResultadoCompra.class)
        );
    }
}
