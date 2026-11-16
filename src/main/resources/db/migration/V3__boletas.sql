
-- V3: Modulo de compras y boletas
-- Responsable: Gabo
-- Requiere V1 (productos) y V2 (usuarios)

CREATE TABLE boleta (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        usuario_id BIGINT NOT NULL,
                        fecha_emision DATETIME(6) NOT NULL,
                        subtotal BIGINT NOT NULL,
                        descuento BIGINT NOT NULL,
                        total BIGINT NOT NULL,

                        PRIMARY KEY (id),

                        CONSTRAINT fk_boleta_usuario
                            FOREIGN KEY (usuario_id)
                                REFERENCES usuario(id),

                        CONSTRAINT chk_boleta_montos
                            CHECK (
                                subtotal >= 0
                                    AND descuento >= 0
                                    AND total = subtotal - descuento
                                )
) ENGINE = InnoDB;


CREATE TABLE detalle_boleta (
                                id BIGINT NOT NULL AUTO_INCREMENT,
                                boleta_id BIGINT NOT NULL,
                                producto_id VARCHAR(20) NOT NULL,
                                cantidad INT NOT NULL,
                                precio_unitario BIGINT NOT NULL,
                                subtotal BIGINT NOT NULL,

                                PRIMARY KEY (id),

                                CONSTRAINT fk_detalle_boleta
                                    FOREIGN KEY (boleta_id)
                                        REFERENCES boleta(id),

                                CONSTRAINT fk_detalle_producto
                                    FOREIGN KEY (producto_id)
                                        REFERENCES producto(id),

                                CONSTRAINT chk_detalle_cantidad
                                    CHECK (cantidad > 0),

                                CONSTRAINT chk_detalle_montos
                                    CHECK (
                                        precio_unitario >= 0
                                            AND subtotal >= 0
                                            AND subtotal = precio_unitario * cantidad
                                        )
) ENGINE = InnoDB;
