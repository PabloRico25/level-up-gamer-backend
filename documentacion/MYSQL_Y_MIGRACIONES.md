# Base de datos y migraciones

Un único esquema InnoDB. Flyway aplica V1 catálogo, V1_1 datos, V2 usuarios, V2_1 tokens, V3 boletas y V3_1 validación de montos. Se conservaron sin cambios las migraciones V1, V1_1 y V3 entregadas por sus autores; las correcciones se agregaron en migraciones nuevas.

| Tabla | Clave y relaciones | Reglas |
|---|---|---|
| categoria | id BIGINT | nombre único |
| producto | id VARCHAR(20), categoria_id → categoria | precio > 0; stock >= 0; activo para baja lógica |
| usuario | id BIGINT | RUN/correo únicos; BCrypt; tipo; activo |
| token_renovacion | usuario_id → usuario | hash único; expiración; revocado |
| boleta | usuario_id → usuario | fecha_emision; subtotal/descuento/total BIGINT; total=subtotal-descuento >=0 |
| detalle_boleta | boleta_id → boleta; producto_id → producto | cantidad >0; precio de compra; subtotal=precio*cantidad |

Relaciones: categoria 1:N producto; usuario 1:N boleta; boleta 1:N detalle_boleta; producto 1:N detalle_boleta; usuario 1:N token_renovacion. No se configura borrado en cascada de las ventas. Usuarios y productos se desactivan.

La compra abre una transacción en RegistrarCompraService. Consulta precios, valida/cotiza, descuenta stock con UPDATE condicional y guarda boleta/detalles. ProductoService y PersistenciaBoletaService exigen transacción MANDATORY. Un error de persistencia después del descuento revierte la compra completa; existe una prueba con FK de usuario inexistente que verifica este punto. Los productos se descuentan ordenados por código para reducir interbloqueos.

El refresh busca token_hash con PESSIMISTIC_WRITE dentro de una transacción, comprueba expiración/revocación/usuario activo, revoca el anterior y crea el siguiente. Dos renovaciones simultáneas del mismo token producen un éxito y un rechazo.

No se migraron datos de bases existentes. Si el equipo necesita conservar usuarios o ventas previas, hacer una copia de respaldo y diseñar una importación revisada a un esquema separado. No usar DROP, ddl-auto=create ni una reparación automática de checksums.
