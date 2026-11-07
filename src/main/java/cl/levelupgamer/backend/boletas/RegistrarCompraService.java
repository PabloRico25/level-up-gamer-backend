
package cl.levelupgamer.backend.boletas;

import cl.levelupgamer.backend.boletas.dto.BoletaResponse;
import cl.levelupgamer.backend.boletas.dto.CrearBoletaRequest;
import cl.levelupgamer.backend.boletas.dto.DetalleBoletaResponse;
import cl.levelupgamer.backend.boletas.dto.ItemCompraRequest;
import cl.levelupgamer.backend.usuarios.Usuario;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RegistrarCompraService {

    private final ValidadorCompra validadorCompra;
    private final CotizadorCompra cotizadorCompra;
    private final PersistenciaBoletaService persistenciaBoletaService;
    private final ObjectProvider<InventarioCompra> inventarioProvider;

    @Transactional
    public BoletaResponse registrar(
            CrearBoletaRequest solicitud,
            Usuario usuario) {

        // Comprobar que exista un usuario
        if (usuario == null || usuario.getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión para comprar"
            );
        }

        // Validar los productos y cantidades
        validadorCompra.validar(solicitud);

        // Buscar la conexión con el inventario
        InventarioCompra inventario = inventarioProvider.getIfAvailable();

        if (inventario == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "El inventario todavía no está disponible"
            );
        }

        // Obtener los datos de cada producto
        Map<String, CotizadorCompra.ProductoDisponible> productos =
                new HashMap<>();

        for (ItemCompraRequest item : solicitud.getItems()) {

            String productoId = item.getProductoId().trim();

            productos.put(
                    productoId,
                    inventario.buscarProducto(productoId)
            );
        }

        // Calcular subtotal, descuento y total
        CotizadorCompra.ResultadoCompra resultado =
                cotizadorCompra.cotizar(
                        solicitud,
                        usuario.getCorreo(),
                        productos
                );

        // Descontar las unidades compradas
        for (DetalleBoletaResponse item : resultado.items()) {

            inventario.descontarStock(
                    item.getProductoId(),
                    item.getCantidad()
            );
        }

        // Guardar boleta y detalles en la misma transacción
        return persistenciaBoletaService.guardar(
                usuario,
                resultado
        );
    }
}
