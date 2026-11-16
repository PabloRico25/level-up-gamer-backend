# Documentación de API LEVEL-UP GAMER

Versión v1. Fecha: 10 de noviembre de 2026. URL local: http://localhost:8080/api/v1. Swagger: http://localhost:8080/swagger-ui.html. OpenAPI JSON: http://localhost:8080/v3/api-docs.

## Endpoints

| Método | Ruta | Descripción | Entrada | Respuestas | Acceso | Autenticación | Roles | Observaciones |
|---|---|---|---|---|---|---|---|---|
| GET | /api/v1/productos | Listar productos | Ninguno | 200 | Pública | No | Público | Productos: solo activos |
| GET | /api/v1/productos/{id} | Consultar detalle | id de ruta | 200, 404 | Pública | No | Público |  |
| POST | /api/v1/productos | Crear registro | id, nombre, categoriaId, precio, stock, stockCritico?, fabricante, imagen?, descripcion? | 201, 400, 401, 403, 409 | Privada | JWT Bearer | ADMINISTRADOR |  |
| PUT | /api/v1/productos/{id} | Editar registro | id, nombre, categoriaId, precio, stock, stockCritico?, fabricante, imagen?, descripcion?; id de ruta | 200, 400, 401, 403, 404, 409 | Privada | JWT Bearer | ADMINISTRADOR | Producto: reemplazo completo; enviar opcionales |
| DELETE | /api/v1/productos/{id} | Desactivar producto | id de ruta | 204, 401, 403, 404, 409 | Privada | JWT Bearer | ADMINISTRADOR | Baja lógica; conserva historial de boletas. |
| GET | /api/v1/categorias | Listar categorias | Ninguno | 200 | Pública | No | Público |  |
| GET | /api/v1/categorias/{id} | Consultar detalle | id de ruta | 200, 404 | Pública | No | Público |  |
| POST | /api/v1/categorias | Crear registro | nombre | 201, 400, 401, 403, 409 | Privada | JWT Bearer | ADMINISTRADOR |  |
| PUT | /api/v1/categorias/{id} | Editar registro | nombre; id de ruta | 200, 400, 401, 403, 404, 409 | Privada | JWT Bearer | ADMINISTRADOR |  |
| DELETE | /api/v1/categorias/{id} | Eliminar categoría | id de ruta | 204, 401, 403, 404, 409 | Privada | JWT Bearer | ADMINISTRADOR | Categoría con productos: 409. |
| POST | /api/v1/auth/registro | Registrar cliente | run, nombre, apellidos, correo, contrasena, fechaNacimiento, region, comuna, direccion | 201, 400, 409 | Pública | No | Público | No acepta elección de rol |
| POST | /api/v1/auth/login | Autenticar | correo, contrasena | 200, 400, 401 | Pública | No | Público | Devuelve accessToken, tokenType, usuario; establece cookie refresh |
| GET | /api/v1/auth/me | Consultar perfil | Authorization Bearer | 200, 401 | Privada | JWT Bearer | Todos los autenticados | id, nombre, correo, tipo |
| POST | /api/v1/auth/refresh | Rotar tokens | Cookie levelup_refresh; Origin permitido; X-Requested-With: XMLHttpRequest | 200, 401, 403 | Privada | Cookie HttpOnly | Cookie válida | Devuelve accessToken y tokenType; reemplaza cookie |
| POST | /api/v1/auth/logout | Revocar renovación | Cookie levelup_refresh; Origin permitido; X-Requested-With: XMLHttpRequest | 204, 403 | Privada | Cookie HttpOnly | Cookie o sesión ya cerrada | Borra cookie; no requiere access token |
| GET | /api/v1/usuarios | Listar usuarios activos | Ninguno | 200, 401, 403 | Privada | JWT Bearer | ADMINISTRADOR | Nunca devuelve contraseñas; no permite quitarse rol ni desactivarse a sí mismo |
| GET | /api/v1/usuarios/{id} | Detalle de usuario | id | 200, 404, 401, 403 | Privada | JWT Bearer | ADMINISTRADOR | Nunca devuelve contraseñas; no permite quitarse rol ni desactivarse a sí mismo |
| POST | /api/v1/usuarios | Crear usuario y rol | Datos de registro más tipo | 201, 400, 409, 401, 403 | Privada | JWT Bearer | ADMINISTRADOR | Nunca devuelve contraseñas; no permite quitarse rol ni desactivarse a sí mismo |
| PUT | /api/v1/usuarios/{id} | Editar usuario | Datos completos más tipo; contrasena null conserva hash | 200, 400, 404, 409, 401, 403 | Privada | JWT Bearer | ADMINISTRADOR | Nunca devuelve contraseñas; no permite quitarse rol ni desactivarse a sí mismo |
| PATCH | /api/v1/usuarios/{id}/rol | Cambiar rol | tipo | 200, 400, 404, 401, 403 | Privada | JWT Bearer | ADMINISTRADOR | Nunca devuelve contraseñas; no permite quitarse rol ni desactivarse a sí mismo |
| DELETE | /api/v1/usuarios/{id} | Desactivar usuario | id | 204, 400, 404, 401, 403 | Privada | JWT Bearer | ADMINISTRADOR | Nunca devuelve contraseñas; no permite quitarse rol ni desactivarse a sí mismo |
| POST | /api/v1/boletas | Registrar compra | items: [{productoId, cantidad}] | 201, 400, 401, 403, 404, 409 | Privada | JWT Bearer | CLIENTE, ADMINISTRADOR | Usuario desde JWT; precio, descuento y total calculados en backend |
| GET | /api/v1/boletas | Listar boletas | Ninguno | 200, 401, 403 | Privada | JWT Bearer | CLIENTE: propias; ADMINISTRADOR y VENDEDOR: todas |  |
| GET | /api/v1/boletas/{id} | Consultar boleta | id | 200, 401, 403, 404 | Privada | JWT Bearer | CLIENTE: propia; ADMINISTRADOR y VENDEDOR: todas |  |
| GET | /api/v1/boletas/mis-compras | Consultar compras propias | Ninguno | 200, 401 | Privada | JWT Bearer | Todos los autenticados |  |
| GET | /api/v1/boletas/usuario/{usuarioId} | Consultar compras de un usuario | usuarioId | 200, 401, 403 | Privada | JWT Bearer | ADMINISTRADOR, VENDEDOR |  |

## Contratos de datos

Los identificadores de producto son String (por ejemplo JM001); los otros identificadores son Long. Montos en pesos chilenos enteros. Fechas ISO 8601. No se devuelven entidades JPA ni hashes de contraseña.

Producto: id, nombre, categoriaId, categoria, precio, stock, stockCritico nullable, fabricante, imagen, descripcion. Precio mayor que cero; stock no negativo. DELETE desactiva; GET de inactivo devuelve 404 y el código sigue reservado.

Usuario administrativo: id, run, nombre, apellidos, correo, fechaNacimiento, region, comuna, direccion, tipo, activo. Registro normaliza RUN y correo, exige mayoría de edad, dominios Duoc/profesor Duoc/Gmail y contraseña mínima de 8 caracteres, máximo 72 bytes UTF-8. PUT conserva contraseña si contrasena es null. El tipo es ADMINISTRADOR, VENDEDOR o CLIENTE.

Boleta: id, usuarioId, fecha, fechaEmision, subtotal, descuento, total, items. fechaEmision se conserva como alias de compatibilidad de fecha. Cada ítem contiene id, boletaId, productoId, cantidad, precioUnitario y subtotal. No enviar usuarioId, precios ni totales al comprar.

Login: accessToken, tokenType=Bearer, usuario={id,nombre,correo,tipo}. La duración de acceso es 30 minutos; no se devuelve expiresIn. Refresh no se entrega en JSON: se recibe en la cookie HttpOnly levelup_refresh, con duración de siete días y ruta /api/v1/auth.

## Errores

Formato: timestamp, status, error, message, path. Validación puede agregar errores=[{campo,mensaje}]. Incluye 401/403 de Security y rechazo de origen. 400 datos inválidos; 401 sesión ausente/vencida; 403 permisos o propiedad; 404 no encontrado; 409 duplicidad o stock insuficiente; 500 mensaje genérico sin trazas. Los errores de protocolo CORS distintos del rechazo de origen pueden ser respondidos por el procesador estándar de Spring.

## Swagger y cookies

Login desde Swagger permite copiar accessToken al botón Authorize, sin escribir el prefijo Bearer manualmente si la interfaz ya lo agrega. Para refresh/logout enviar X-Requested-With=XMLHttpRequest y un Origin permitido. Si Swagger no envía Origin al hacer una petición del mismo origen, probar renovación desde React o un cliente HTTP que incluya las cabeceras; la comprobación de origen es deliberada. No pegar tokens en documentación ni capturas.
