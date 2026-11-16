package cl.levelupgamer.backend.boletas;
import cl.levelupgamer.backend.productos.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class InventarioProductos implements InventarioCompra {
 private final ProductoService productos;
 public CotizadorCompra.ProductoDisponible buscarProducto(String id) {
  var p=productos.buscarPorId(id);
  return new CotizadorCompra.ProductoDisponible(p.precio(),p.stock(),true);
 }
 public void descontarStock(String id,int cantidad) { productos.descontarStock(id,cantidad); }
}
