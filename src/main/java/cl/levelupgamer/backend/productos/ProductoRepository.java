package cl.levelupgamer.backend.productos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, String> {

    /** Solo los productos activos, con su categoría cargada en la misma consulta. */
    @Query("select p from Producto p join fetch p.categoria where p.activo = true order by p.id")
    List<Producto> listarActivos();

    @Query("select p from Producto p join fetch p.categoria where p.id = :id and p.activo = true")
    Optional<Producto> buscarActivoPorId(@Param("id") String id);

    /** Cuenta también los desactivados: la clave foránea los sigue apuntando. */
    boolean existsByCategoriaId(Long categoriaId);

    /**
     * Descuenta stock con una sola sentencia condicional: solo cambia la fila si el producto está activo y alcanza el stock.
     * Devuelve cuántas filas cambió (1 = descontó, 0 = no existe, está desactivado o no alcanza).
     * Así dos compras simultáneas nunca dejan el stock negativo, sin leer y escribir en dos pasos.
     */
    @Modifying(flushAutomatically = true)
    @Query("update Producto p set p.stock = p.stock - :cantidad where p.id = :id and p.activo = true and p.stock >= :cantidad")
    int descontarStock(@Param("id") String id, @Param("cantidad") int cantidad);

    /** Devuelve stock. No exige que esté activo: una boleta antigua puede apuntar a un producto dado de baja. */
    @Modifying(flushAutomatically = true)
    @Query("update Producto p set p.stock = p.stock + :cantidad where p.id = :id")
    int reponerStock(@Param("id") String id, @Param("cantidad") int cantidad);
}