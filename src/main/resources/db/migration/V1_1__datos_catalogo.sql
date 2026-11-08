-- Datos de ejemplo del catálogo: las 8 categorías y los 10 productos originales del frontend (src/data/productos.js).

INSERT INTO categoria (id, nombre) VALUES
    (1, 'Juegos de Mesa'),
    (2, 'Accesorios'),
    (3, 'Consolas'),
    (4, 'Computadores Gamers'),
    (5, 'Sillas Gamers'),
    (6, 'Mouse'),
    (7, 'Mousepad'),
    (8, 'Poleras Personalizadas');

INSERT INTO producto (id, nombre, categoria_id, precio, stock, stock_critico, fabricante, imagen, descripcion, activo) VALUES
    ('JM001', 'Catan', 1, 29990, 12, NULL, 'CATAN Studio', '/assets/img/catan.jpg', 'Un clásico juego de estrategia donde los jugadores compiten por colonizar y expandirse en la isla de Catan. Ideal para 3 a 4 jugadores.', TRUE),
    ('JM002', 'Carcassonne', 1, 24990, 9, NULL, 'Z-Man Games', '/assets/img/carcassonne.jpg', 'Juego de colocación de fichas donde los jugadores construyen el paisaje alrededor de la fortaleza medieval de Carcassonne.', TRUE),
    ('AC001', 'Control Inalámbrico Xbox Series X', 2, 59990, 15, NULL, 'Microsoft', '/assets/img/control-xbox.jpg', 'Control inalámbrico compatible con consolas Xbox y PC, con respuesta táctil mejorada y botones mapeables.', TRUE),
    ('AC002', 'Auriculares Gamer HyperX Cloud II', 2, 79990, 8, NULL, 'HyperX', '/assets/img/hyperx-cloud2.jpg', 'Auriculares con sonido envolvente, micrófono desmontable y almohadillas diseñadas para largas sesiones de juego.', TRUE),
    ('CO001', 'PlayStation 5', 3, 549990, 5, NULL, 'Sony', '/assets/img/ps5.jpg', 'Consola de última generación de Sony con gráficos de alta calidad y tiempos de carga ultrarrápidos.', TRUE),
    ('CG001', 'PC Gamer ASUS ROG Strix', 4, 1299990, 4, NULL, 'ASUS', '/assets/img/asus-rog-strix.jpg', 'Equipo gamer de alto rendimiento diseñado para ofrecer una experiencia fluida en juegos exigentes.', TRUE),
    ('SG001', 'Silla Gamer Secretlab Titan', 5, 349990, 7, NULL, 'Secretlab', '/assets/img/secretlab-titan.jpg', 'Silla gamer ergonómica y ajustable, diseñada para mantener la comodidad durante sesiones prolongadas.', TRUE),
    ('MS001', 'Mouse Gamer Logitech G502 HERO', 6, 49990, 18, NULL, 'Logitech', '/assets/img/logitech-g502.jpg', 'Mouse gamer con sensor de alta precisión y botones personalizables para un mayor control.', TRUE),
    ('MP001', 'Mousepad Razer Goliathus Extended Chroma', 7, 29990, 20, NULL, 'Razer', '/assets/img/razer-goliathus.jpg', 'Mousepad extendido con superficie uniforme e iluminación RGB personalizable.', TRUE),
    ('PP001', 'Polera Gamer Personalizada Level-Up', 8, 14990, 25, NULL, 'Level-Up Gamer', '/assets/img/polera-levelup.jpg', 'Polera gamer personalizable con gamer tag o diseño favorito.', TRUE);