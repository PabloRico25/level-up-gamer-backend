# LEVEL-UP GAMER — Backend

API REST de la tienda LEVEL-UP GAMER (Evaluación Parcial 3, Desarrollo Fullstack II, Duoc UC). Spring Boot 4, Spring Data JPA, MySQL, Flyway y Swagger. El frontend está en el repositorio `level-up-gamer-react`.

## Requisitos

- Java 21
- MySQL 8 (o superior) en el computador
- No hace falta instalar Maven: el proyecto trae `mvnw`

## Primera vez

1. Crear la base y un usuario propio (en MySQL Workbench o en la consola de MySQL):

```sql
CREATE DATABASE levelup CHARACTER SET utf8mb4;
CREATE USER 'levelup_user'@'localhost' IDENTIFIED BY 'elige-tu-propia-clave';
GRANT ALL PRIVILEGES ON levelup.* TO 'levelup_user'@'localhost';
```

2. Copiar `.env.example` como `.env` en la raíz de este proyecto y poner la clave elegida en `DB_PASSWORD`. El archivo `.env` no se sube a Git.
3. Arrancar:

```powershell
.\mvnw spring-boot:run
```

Al arrancar, Flyway crea las tablas y carga los datos de ejemplo (8 categorías y 10 productos).

## Configuración

Todo está en `src/main/resources/application.properties`. Las claves no se escriben ahí: se leen del archivo `.env` o de variables de entorno.

| Variable | Para qué sirve | Valor por defecto |
|---|---|---|
| `DB_URL` | Dirección de la base MySQL | `jdbc:mysql://localhost:3306/levelup?serverTimezone=America/Santiago` |
| `DB_USER` | Usuario de la base | `levelup_user` |
| `DB_PASSWORD` | Clave de la base | ninguno: es obligatoria |
| `CORS_ALLOWED_ORIGINS` | Orígenes del frontend permitidos, separados por coma | `http://localhost:5173` |

## Rutas útiles

| Qué | Dirección |
|---|---|
| Swagger | http://localhost:8080/swagger-ui.html |
| Definición OpenAPI | http://localhost:8080/v3/api-docs |

Swagger trae el botón **Authorize**: cuando exista el inicio de sesión, ahí se pega el token JWT para probar las rutas protegidas.

## Pruebas

```powershell
.\mvnw test
```

Corren las pruebas del formato de errores, que no necesitan base de datos, y la que arranca la aplicación completa, que sí necesita MySQL arriba y el `.env` listo.

## Cómo está organizado

```
src/main/java/cl/levelupgamer/backend/
├─ config/    Swagger (OpenAPI) y CORS
├─ comun/     Formato de errores y excepciones compartidas
└─ usuarios/  Usuarios
src/main/resources/db/migration/   Migraciones de Flyway
src/test/java/                     Pruebas, un paquete por módulo
```

Los demás módulos (categorías, productos, autenticación y boletas) se agregan en los siguientes Pull Requests.

## Migraciones

El esquema de la base se construye solo con los archivos de `db/migration`: `V1__catalogo.sql` crea las tablas de categoría y producto, y `V1_1__datos_catalogo.sql` carga los datos de ejemplo. Cada integrante agrega las suyas con su propio rango de números (V1 catálogo, V2 usuarios, V3 boletas).

Una migración que ya está en `main` no se edita: se agrega otra. Flyway guarda una huella del texto de cada migración aplicada; si cambia aunque sea un espacio, al arrancar avisa `Migration checksum mismatch`. Si tu base local solo tiene datos de ejemplo, se recrea con `DROP DATABASE levelup; CREATE DATABASE levelup CHARACTER SET utf8mb4;` y al arrancar se reconstruye.

## Problemas conocidos

- **Error 429 al descargar Maven o dependencias:** Maven Central limita las descargas de algunas redes. Se puede esperar un rato o usar el espejo de Google, agregando esto a `%USERPROFILE%\.m2\settings.xml` (el `id` tiene que ser exactamente `central-google`):

```xml
<settings>
  <mirrors>
    <mirror>
      <id>central-google</id>
      <mirrorOf>central</mirrorOf>
      <url>https://maven-central.storage-download.googleapis.com/maven2/</url>
    </mirror>
  </mirrors>
</settings>
```