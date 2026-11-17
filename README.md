# level-up-gamer-backend

API REST de la tienda **LEVEL-UP GAMER**. Proyecto de Desarrollo Fullstack II (DSY1104), Duoc UC.

Equipo: Cristian Flores, Gabriel Bustamante, Pablo Rico y Bastián Díaz.
Repositorio hermano: [`level-up-gamer-frontend`](https://github.com/PabloRico25/level-up-gamer-frontend) (React).

## Qué hace

- **Catálogo:** categorías y productos con CRUD, validaciones y control de stock.
- **Usuarios y seguridad:** registro, login con JWT, renovación de sesión por cookie y tres roles: `ADMINISTRADOR`, `VENDEDOR` y `CLIENTE`.
- **Compras:** boletas con su detalle. La compra descuenta el stock en una sola transacción y aplica un 20 % de descuento a correos `@duoc.cl` y `@profesor.duoc.cl`.
- **Documentación:** Swagger con esquema Bearer. Todas las rutas están bajo `/api/v1`.

Tecnologías: Java 21, Spring Boot 4.1.2, Spring Security (JWT), MySQL 8.4, Flyway y Swagger (springdoc).

## Qué puede hacer cada rol

| Rol | Puede |
|---|---|
| Sin sesión | Ver productos y categorías, registrarse e iniciar sesión |
| `CLIENTE` | Lo anterior, comprar y ver sus propias boletas |
| `VENDEDOR` | Ver productos y órdenes (solo lectura) |
| `ADMINISTRADOR` | Administrar productos, categorías y usuarios, y ver todas las órdenes |

## Requisitos

- JDK 21 o superior.
- MySQL 8.4.
- Maven no hace falta instalarlo: el proyecto incluye `mvnw` (Windows: `mvnw.cmd`).

## Puesta en marcha

### 1. Base de datos

Crear una base **nueva** llamada `levelup_final` y un usuario con permisos solo sobre ella. No reutilizar ni borrar bases existentes. Ejemplo, entrando a MySQL como administrador:

```sql
CREATE DATABASE levelup_final CHARACTER SET utf8mb4;
CREATE USER 'levelup'@'localhost' IDENTIFIED BY 'una_clave_solo_con_letras_y_numeros';
GRANT ALL PRIVILEGES ON levelup_final.* TO 'levelup'@'localhost';
```

Usar para la clave solo letras y números: símbolos como `$`, `\`, `#` o comillas se corrompen entre el archivo `.env` y la conexión.

Las tablas las crea Flyway al arrancar (6 migraciones en `src/main/resources/db/migration`). Una migración ya aplicada nunca se edita.

### 2. Configuración (`.env`)

Copiar `.env.example` a `.env` en la raíz del proyecto y completar:

| Variable | Qué poner |
|---|---|
| `DB_URL` | Ya viene con `jdbc:mysql://localhost:3306/levelup_final` |
| `DB_USER` y `DB_PASSWORD` | El usuario y la clave del paso anterior, sin comillas |
| `JWT_SECRET` | Una clave aleatoria en **Base64** de al menos 32 bytes. Generarla con `openssl rand -base64 32`. Un texto cualquiera no sirve y el backend no arranca |

El archivo `.env` está ignorado por git. Nunca se sube.

### 3. Primer administrador

No existen credenciales predeterminadas. Para crear el primer administrador, agregar **temporalmente** al `.env`:

```
app.bootstrap.enabled=true
ADMIN_CORREO=correo@gmail.com
ADMIN_PASSWORD=clave_de_8_o_mas_caracteres
ADMIN_RUN=12345678-5
```

- El correo debe terminar en `@gmail.com`, `@duoc.cl` o `@profesor.duoc.cl`.
- El RUN debe tener dígito verificador válido.
- La clave debe tener entre 8 caracteres y 72 bytes.

Iniciar el backend una vez y **retirar esas cuatro líneas** del `.env`. Si el administrador ya existe no se vuelve a crear. Si se cambia el correo pero se deja el mismo RUN, el arranque falla con `RUN ya registrado`.

Los demás usuarios (vendedores, clientes) se crean desde el panel de administración del frontend.

### 4. Arrancar

Desde IntelliJ IDEA: abrir la carpeta del proyecto, elegir el JDK 21 en *File → Project Structure → SDK*, abrir `LevelUpGamerBackendApplication` y ejecutarla con el triángulo verde. Para detenerla, usar el botón **Stop**.

Desde la terminal:

```
./mvnw spring-boot:run
```

(en Windows: `mvnw.cmd spring-boot:run`). Está listo cuando la consola muestra `Started LevelUpGamerBackendApplication`.

- API: `http://localhost:8080/api/v1`
- Swagger: `http://localhost:8080/swagger-ui/index.html`. El botón **Authorize** recibe el `accessToken` que entrega `POST /api/v1/auth/login`.

## Pruebas

- **Unitarias** (no necesitan base de datos): `./mvnw test`
- **Integración** (crean y eliminan datos de prueba): `./mvnw verify -DexcludedGroups=ninguno`. Ejecutarlas **solo** con `DB_URL` apuntando a una base de pruebas nueva, nunca a la base de uso. `JWT_SECRET` debe estar configurado.

## Documentación

En `documentacion/`:

- `ERS.docx`: especificación de requisitos del sistema.
- `APIS_E_INTEGRACION.docx`: documento de APIs e integración, con el modelo de datos, los endpoints, las pruebas y las capturas de la comunicación frontend-backend.
- `API.md` y `API.xlsx`: rutas, métodos, roles y respuestas.
- `INTEGRACION.md`: cómo se conecta el frontend con esta API.
- `MYSQL_Y_MIGRACIONES.md`: esquema de la base y migraciones.
- `endpoints.json` y `pruebas-http.json`: comprobaciones HTTP.

## Estructura

```
src/main/java/cl/levelupgamer/backend/
  auth/         login, renovación y cierre de sesión
  boletas/      compras, boletas y descuento Duoc
  categorias/   categorías
  productos/    productos y stock
  usuarios/     usuarios, roles y administrador inicial
  config/       seguridad, CORS y OpenAPI
  comun/        errores comunes y utilidades
```

## Notas

- **CORS y cookies:** el CORS local admite `localhost:5173` y `127.0.0.1:5173`. Usar el mismo host (`localhost` con `localhost`) en el frontend y la API.
- **HTTP local:** `COOKIE_SECURE=false` es solo para HTTP local. En HTTPS debe ser `true`.
- **Pagos:** el sistema registra la boleta y descuenta stock, pero no procesa pagos bancarios ni despacho.
- **Despliegue:** este backend no está publicado; corre en local.
