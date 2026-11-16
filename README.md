# level-up-gamer-backend

Proyecto de Desarrollo Fullstack II (DSY1104), Duoc UC. Equipo: Cristian Flores, Gabriel Bustamante, Pablo Rico y Bastián Díaz.
Repositorio hermano: `level-up-gamer-frontend` (React).

Spring Boot 4.1.2, Java 21, MySQL 8.4, Flyway, JWT y Swagger. Catálogo, usuarios y compras integrados.

1. Instalar JDK 21+ y MySQL 8.4.
2. Crear una base NUEVA levelup_final y un usuario con permisos de esquema y datos solo en ella. No reutilizar ni borrar bases existentes.
3. Copiar .env.example a .env y completar DB_URL, DB_USER, DB_PASSWORD y JWT_SECRET. Generar la clave local con openssl rand -base64 32; no publicarla. No poner comillas de shell en el archivo de propiedades.
4. Ejecutar ./mvnw test y ./mvnw spring-boot:run (Windows: ./mvnw.cmd).
5. Abrir http://localhost:8080/swagger-ui.html. API bajo /api/v1.

Para el primer administrador agregar temporalmente app.bootstrap.enabled=true, ADMIN_CORREO, ADMIN_PASSWORD y ADMIN_RUN al .env; usar correo permitido, RUN válido y contraseña de 8+ caracteres/72 bytes máximo. Iniciar una vez y retirar esas propiedades. No hay credenciales predeterminadas; una cuenta existente no se eleva automáticamente.

Pruebas con BD: ./mvnw verify -DexcludedGroups=ninguno, SOLO con DB_URL apuntando a una base de pruebas nueva. Las pruebas crean/eliminan fixtures. JWT_SECRET debe estar configurado. Las migraciones se validan; nunca editar una aplicada. Consultar documentacion/ para API, esquema y pruebas.

CORS local admite localhost:5173 y 127.0.0.1:5173; usar el mismo host para frontend/API. COOKIE_SECURE=false es solo para HTTP local; en HTTPS usar true. No se publicó el backend.
