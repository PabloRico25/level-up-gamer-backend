-- Catálogo: categoria y producto. Dueño: Alex. Una migración que ya está en main no se edita: se agrega otra.

CREATE TABLE categoria (
                           id     BIGINT      NOT NULL AUTO_INCREMENT,
                           nombre VARCHAR(60) NOT NULL,
                           PRIMARY KEY (id),
                           CONSTRAINT uk_categoria_nombre UNIQUE (nombre)
) ENGINE = InnoDB;

CREATE TABLE producto (
                          id            VARCHAR(20)  NOT NULL,
                          nombre        VARCHAR(100) NOT NULL,
                          categoria_id  BIGINT       NOT NULL,
                          precio        BIGINT       NOT NULL,
                          stock         INT          NOT NULL,
                          stock_critico INT          NULL,
                          fabricante    VARCHAR(100) NOT NULL,
                          imagen        VARCHAR(255) NULL,
                          descripcion   VARCHAR(500) NULL,
                          activo        BOOLEAN      NOT NULL DEFAULT TRUE,
                          PRIMARY KEY (id),
                          CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id),
                          CONSTRAINT chk_producto_precio CHECK (precio > 0),
                          CONSTRAINT chk_producto_stock CHECK (stock >= 0),
                          CONSTRAINT chk_producto_stock_critico CHECK (stock_critico IS NULL OR stock_critico >= 0)
) ENGINE = InnoDB;