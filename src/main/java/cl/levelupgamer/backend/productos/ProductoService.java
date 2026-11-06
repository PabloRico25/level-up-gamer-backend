package cl.levelupgamer.backend.productos;

import cl.levelupgamer.backend.categorias.Categoria;
import cl.levelupgamer.backend.categorias.CategoriaRepository;
import cl.levelupgamer.backend.comun.ConflictoException;
import cl.levelupgamer.backend.comun.RecursoNoEncontradoException;
import cl.levelupgamer.backend.comun.SolicitudInvalidaException;
import cl.levelupgamer.backend.productos.dto.ProductoRequest;
import cl.levelupgamer.backend.productos.dto.ProductoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {
        return productoRepository.listarActivos().stream()
                .map(ProductoResponse::desde)
                .toList();
    }

    /** 404 si no existe o está desactivado. */
    @Transactional(readOnly = true)
    public ProductoResponse buscarPorId(String id) {
        return ProductoResponse.desde(obtenerActivo(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        if (request.id() == null || request.id().isBlank()) {
            throw new SolicitudInvalidaException("El código del producto es obligatorio");
        }
        // Un código desactivado sigue ocupado: las boletas antiguas lo apuntan.
        if (productoRepository.existsById(request.id())) {
            log.warn("Intento de crear un producto con codigo repetido: {}", request.id());
            throw new ConflictoException("Ya existe un producto con el código " + request.id());
        }
        Producto producto = new Producto();
        producto.setId(request.id());
        copiarDatos(producto, request);
        Producto guardado = productoRepository.save(producto);
        log.info("Producto creado: {}", guardado.getId());
        return ProductoResponse.desde(guardado);
    }

    @Transactional
    public ProductoResponse actualizar(String id, ProductoRequest request) {
        Producto producto = obtenerActivo(id);
        copiarDatos(producto, request);
        log.info("Producto actualizado: {}", id);
        return ProductoResponse.desde(producto);
    }

    /** DELETE no borra la fila: deja el producto desactivado para no romper las boletas antiguas. */
    @Transactional
    public void desactivar(String id) {
        obtenerActivo(id).setActivo(false);
        log.info("Producto desactivado: {}", id);
    }

    /**
     * Contrato del stock con las compras (CONTRATO_API.md, sección 7). Descuenta de forma atómica.
     * Exige que quien llama ya tenga una transacción abierta (la de BoletaService.crear): así, si otro producto de la
     * misma compra no tiene stock, todos los descuentos se revierten juntos. Sin transacción lanza una excepción
     * en vez de dejar una compra a medias.
     * No actualiza los objetos Producto que quien llama ya haya cargado: para el precio se usa buscarPorId antes de descontar.
     *
     * @throws SolicitudInvalidaException     si la cantidad no es mayor que 0 (400)
     * @throws RecursoNoEncontradoException   si el producto no existe o está desactivado (404)
     * @throws ConflictoException             si no alcanza el stock (409)
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void descontarStock(String productoId, int cantidad) {
        exigirCantidadPositiva(cantidad);
        if (productoRepository.descontarStock(productoId, cantidad) == 0) {
            // Ninguna fila cambió: se distingue "no existe" de "no alcanza" para dar el error correcto.
            Producto producto = obtenerActivo(productoId);
            log.warn("Stock insuficiente para {}: quedan {} y se pidieron {}", productoId, producto.getStock(), cantidad);
            throw new ConflictoException("Stock insuficiente para " + producto.getNombre() + ": quedan "
                    + producto.getStock() + " y se pidieron " + cantidad);
        }
    }

    /**
     * Devuelve stock (por ejemplo, si se anula una boleta). Mismas reglas de transacción que descontarStock.
     * Funciona también con productos desactivados.
     *
     * @throws SolicitudInvalidaException   si la cantidad no es mayor que 0 (400)
     * @throws RecursoNoEncontradoException si el producto no existe (404)
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void reponerStock(String productoId, int cantidad) {
        exigirCantidadPositiva(cantidad);
        if (productoRepository.reponerStock(productoId, cantidad) == 0) {
            throw new RecursoNoEncontradoException("No existe el producto " + productoId);
        }
    }

    private void exigirCantidadPositiva(int cantidad) {
        if (cantidad <= 0) {
            throw new SolicitudInvalidaException("La cantidad debe ser mayor que 0");
        }
    }

    private Producto obtenerActivo(String id) {
        return productoRepository.buscarActivoPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el producto " + id));
    }

    private void copiarDatos(Producto producto, ProductoRequest request) {
        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new SolicitudInvalidaException("La categoría " + request.categoriaId() + " no existe"));
        producto.setNombre(request.nombre().trim());
        producto.setCategoria(categoria);
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        producto.setStockCritico(request.stockCritico());
        producto.setFabricante(request.fabricante().trim());
        producto.setImagen(request.imagen());
        producto.setDescripcion(request.descripcion());
    }
}