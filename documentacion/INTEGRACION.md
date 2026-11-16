# Integración backend y frontend

LEVEL-UP GAMER son dos aplicaciones que se comunican por una API REST versionada (`/api/v1`): este backend (Spring Boot) y el frontend de React (repositorio `level-up-gamer-frontend`). El documento completo de entrega es `APIS_E_INTEGRACION.docx`; aquí va el resumen.

## Origen del código

El catálogo (categorías, productos, Swagger, errores y control de stock) lo desarrolló Pablo Rico. Usuarios, registro, JWT, sesión y roles son de Cristian Flores. Las boletas y las compras son de Gabriel Bustamante. Bastián Díaz apoyó en el frontend. Las tres partes del backend se integraron en una base común con migraciones Flyway.

## Flujo de una solicitud

1. React llama a una función de `src/api/` o `src/services/`.
2. `api/cliente.js` usa `fetch`; en rutas protegidas agrega `Authorization: Bearer <token>` y, ante un 401, renueva la sesión una vez y repite la petición.
3. Spring Security valida el JWT y consulta en MySQL el rol y el estado actuales del usuario.
4. El controlador valida la entrada y llama al servicio, que aplica la lógica de negocio.
5. El repositorio escribe en MySQL. La compra es una transacción: si algo falla, no queda boleta ni se descuenta stock.
6. La respuesta vuelve en JSON; los errores usan un formato común (`timestamp`, `status`, `error`, `message`, `path`).

## Seguridad

- Contraseñas con BCrypt.
- JWT de 30 minutos (HS256) guardado en memoria en el frontend.
- Cookie de renovación HttpOnly, SameSite=Strict, de 7 días, con rotación; en MySQL solo se guarda su hash.
- Roles ADMINISTRADOR, VENDEDOR y CLIENTE, leídos de MySQL en cada petición: un cambio de rol o una baja surte efecto aunque el token ya esté emitido.
- Un cliente solo puede ver sus propias boletas.

## Contratos resueltos

- Las rutas de lectura de productos y categorías son públicas; escribir es solo ADMINISTRADOR.
- Comprar es solo CLIENTE y ADMINISTRADOR; VENDEDOR consulta productos y órdenes.
- El precio y el stock definitivos se validan siempre en el servidor.
- Los orígenes de CORS son configurables; un origen no permitido recibe 403 en JSON.

## Documentación de la API

Swagger: `http://localhost:8080/swagger-ui.html`. La planilla `API.xlsx` y `API.md` listan los 26 endpoints con su acceso y roles.
