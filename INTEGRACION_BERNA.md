
# LEVEL-UP GAMER — Integración del módulo Berna

## 1. Responsabilidad
Berna desarrolla:
- Registro de usuarios
- Login con JWT
- Renovación y cierre de sesión
- Seguridad y roles
- Administración de usuarios

## 2. Archivos del backend

Paquete base: cl.levelupgamer.backend

auth/
- AuthController.java
- JwtService.java
- LoginRequest.java
- LoginResponse.java
- TokenRenovacion.java
- TokenRenovacionRepository.java
- TokenRenovacionService.java
- UsuarioDetailsService.java

config/
- AutenticacionConfig.java
- CorsConfig.java
- JwtConfig.java
- PasswordConfig.java
- SecurityConfig.java

usuarios/
- RegistroController.java
- RegistroRequest.java
- RolUsuario.java
- Usuario.java
- UsuarioController.java
- UsuarioRepository.java
- UsuarioService.java
- ValidadorRun.java

## 3. Dependencias necesarias

- Spring Web MVC
- Spring Data JPA
- Spring Security
- OAuth2 Resource Server (JWT)
- Validation
- MySQL Connector
- Lombok

Desarrollado con Java 21 y Spring Boot 4.1.2.

## 4. Tablas MySQL

- usuario
- token_renovacion

El esquema definitivo debe coordinarse con Alex.
No usar ddl-auto=update como estrategia final de producción.

## 5. Endpoints

POST  /api/v1/auth/registro
POST  /api/v1/auth/login
POST  /api/v1/auth/refresh
POST  /api/v1/auth/logout
GET   /api/v1/auth/me

GET   /api/v1/usuarios
PATCH /api/v1/usuarios/{id}/rol

## 6. Roles

ADMINISTRADOR: administración completa.
VENDEDOR: consultas autorizadas, sin modificar productos.
CLIENTE: acceso a la tienda y compras autorizadas.

Los permisos deben aplicarse en Spring Security,
no solamente ocultando botones en React.

## 7. Configuraciones

Variables de entorno:
- MYSQL_PASSWORD
- JWT_SECRET

No subir claves reales ni contraseñas a GitHub.

Revisar y combinar SecurityConfig, CorsConfig,
application.properties y pom.xml con el backend de Alex.

## 8. Frontend

Archivos principales modificados:
- src/services/authApi.js
- src/services/usuariosApi.js
- src/context/UsuariosContext.jsx
- src/pages/Login.jsx
- src/pages/Registro.jsx
- src/pages/AdminUsuarios.jsx
- src/pages/Admin.jsx
- src/components/navbar.jsx
- src/components/AdminLayout.jsx

También se actualizaron App.jsx y las pantallas
de administración de productos.

## 9. Pruebas

Comprobado:
- Registro
- Login
- Recuperación de sesión
- Cierre de sesión
- Panel Administrador
- Permisos visuales del Vendedor
- Rechazo 401 sin JWT

Pendiente:
- Prueba 403 de permisos directamente en la API
- Revisión completa de seguridad y CSRF
- Pruebas de integración con los módulos de Alex y Gabo
- Swagger y documentación final
