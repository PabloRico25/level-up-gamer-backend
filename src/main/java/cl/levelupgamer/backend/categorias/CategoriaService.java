package cl.levelupgamer.backend.categorias;

import cl.levelupgamer.backend.categorias.dto.CategoriaRequest;
import cl.levelupgamer.backend.categorias.dto.CategoriaResponse;
import cl.levelupgamer.backend.comun.ConflictoException;
import cl.levelupgamer.backend.comun.RecursoNoEncontradoException;
import cl.levelupgamer.backend.productos.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAllByOrderByIdAsc().stream()
                .map(CategoriaResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscarPorId(Long id) {
        return CategoriaResponse.desde(obtener(id));
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        String nombre = request.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            log.warn("Intento de crear una categoria repetida: {}", nombre);
            throw new ConflictoException("Ya existe la categoría " + nombre);
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        Categoria guardada = categoriaRepository.save(categoria);
        log.info("Categoria creada: {} (id={})", guardada.getNombre(), guardada.getId());
        return CategoriaResponse.desde(guardada);
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = obtener(id);
        String nombre = request.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            log.warn("Intento de renombrar la categoria {} a un nombre repetido: {}", id, nombre);
            throw new ConflictoException("Ya existe la categoría " + nombre);
        }
        categoria.setNombre(nombre);
        log.info("Categoria actualizada: id={} nombre={}", id, nombre);
        return CategoriaResponse.desde(categoria);
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = obtener(id);
        // Cuenta también los productos desactivados: la clave foránea los sigue apuntando.
        if (productoRepository.existsByCategoriaId(id)) {
            log.warn("Intento de eliminar la categoria {} que todavia tiene productos", id);
            throw new ConflictoException("No se puede eliminar la categoría " + categoria.getNombre()
                    + " porque tiene productos");
        }
        categoriaRepository.delete(categoria);
        log.info("Categoria eliminada: id={}", id);
    }

    private Categoria obtener(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la categoría " + id));
    }
}