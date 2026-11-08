package cl.levelupgamer.backend.productos;

import cl.levelupgamer.backend.categorias.Categoria;
import cl.levelupgamer.backend.categorias.CategoriaRepository;
import cl.levelupgamer.backend.comun.ConflictoException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Prueba con MySQL real del contrato del stock (CONTRATO_API.md, sección 7).
 * Necesita la base local arriba y el archivo .env; no corre por defecto. Para correrla:
 * ./mvnw verify -DexcludedGroups=ninguno
 * Crea sus propios productos (ZT001 y ZT002) y los borra al terminar.
 */
@Tag("integracion")
@SpringBootTest
class ProductoStockIntegracionTests {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transaccion;

    @BeforeEach
    void crearProductosDePrueba() {
        transaccion = new TransactionTemplate(transactionManager);
        Categoria categoria = categoriaRepository.findAllByOrderByIdAsc().get(0);
        productoRepository.save(nuevo("ZT001", categoria, 5));
        productoRepository.save(nuevo("ZT002", categoria, 1));
    }

    @AfterEach
    void borrarProductosDePrueba() {
        productoRepository.deleteAllById(List.of("ZT001", "ZT002"));
    }

    private static Producto nuevo(String id, Categoria categoria, int stock) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre("Producto de prueba " + id);
        producto.setCategoria(categoria);
        producto.setPrecio(1000L);
        producto.setStock(stock);
        producto.setFabricante("Prueba");
        return producto;
    }

    private int stockDe(String id) {
        return productoRepository.findById(id).orElseThrow().getStock();
    }

    @Test
    @DisplayName("Varios clientes al mismo tiempo nunca dejan el stock negativo")
    void variosClientesAlMismoTiempoNuncaDejanElStockNegativo() throws Exception {
        // Given: hay 5 en stock y 12 clientes compran 1 a la vez
        int clientes = 12;
        ExecutorService pool = Executors.newFixedThreadPool(clientes);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<Boolean>> resultados = new ArrayList<>();
        for (int i = 0; i < clientes; i++) {
            resultados.add(pool.submit(() -> {
                salida.await();
                try {
                    transaccion.executeWithoutResult(estado -> productoService.descontarStock("ZT001", 1));
                    return true;
                } catch (ConflictoException sinStock) {
                    return false;
                }
            }));
        }

        // When: todos parten juntos
        salida.countDown();
        int compras = 0;
        for (Future<Boolean> resultado : resultados) {
            if (resultado.get(30, TimeUnit.SECONDS)) {
                compras++;
            }
        }
        pool.shutdown();

        // Then: ganan exactamente 5 y el stock queda en 0
        assertEquals(5, compras);
        assertEquals(0, stockDe("ZT001"));
    }

    @Test
    @DisplayName("Si un producto de la compra no tiene stock, se revierte todo")
    void siUnProductoDeLaCompraNoTieneStockSeRevierteTodo() {
        // Given: ZT001 alcanza (5) y ZT002 no (1 en stock, se piden 3)
        // When y Then
        assertThrows(ConflictoException.class, () -> transaccion.executeWithoutResult(estado -> {
            productoService.descontarStock("ZT001", 2);
            productoService.descontarStock("ZT002", 3);
        }));

        // Then: ningun descuento quedo guardado
        assertEquals(5, stockDe("ZT001"));
        assertEquals(1, stockDe("ZT002"));
    }

    @Test
    @DisplayName("Una compra correcta descuenta ambos productos")
    void unaCompraCorrectaDescuentaAmbosProductos() {
        // When
        transaccion.executeWithoutResult(estado -> {
            productoService.descontarStock("ZT001", 2);
            productoService.descontarStock("ZT002", 1);
        });

        // Then
        assertEquals(3, stockDe("ZT001"));
        assertEquals(0, stockDe("ZT002"));
    }

    @Test
    @DisplayName("Reponer stock suma y funciona con productos desactivados")
    void reponerStockSumaYFuncionaConProductosDesactivados() {
        // Given: un producto desactivado
        productoService.desactivar("ZT001");

        // When
        transaccion.executeWithoutResult(estado -> productoService.reponerStock("ZT001", 4));

        // Then
        assertEquals(9, stockDe("ZT001"));
    }

    @Test
    @DisplayName("Sin transaccion abierta se rechaza el descuento")
    void sinTransaccionAbiertaSeRechazaElDescuento() {
        // When y Then: evita compras a medias (la compra debe ser @Transactional)
        assertThrows(IllegalTransactionStateException.class, () -> productoService.descontarStock("ZT001", 1));
        assertEquals(5, stockDe("ZT001"));
    }
}