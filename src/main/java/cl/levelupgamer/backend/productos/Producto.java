package cl.levelupgamer.backend.productos;

import cl.levelupgamer.backend.categorias.Categoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla producto. El id es el código de texto que ya usa el frontend (por ejemplo JM001) y lo pone quien crea el producto.
 * "activo" en falso significa que el producto fue dado de baja: no se muestra, pero las boletas antiguas lo siguen apuntando.
 */
@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
public class Producto {

    @Id
    @Column(length = 20)
    private String id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    /** Pesos chilenos, entero. */
    @Column(nullable = false)
    private Long precio;

    @Column(nullable = false)
    private Integer stock;

    /** Opcional: aviso de poco stock. */
    @Column(name = "stock_critico")
    private Integer stockCritico;

    @Column(nullable = false, length = 100)
    private String fabricante;

    @Column(length = 255)
    private String imagen;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private boolean activo = true;
}