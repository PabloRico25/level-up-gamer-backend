package cl.levelupgamer.backend.usuarios;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name="bearerAuth")
@RestController
@Tag(name = "Usuarios", description = "Administración de usuarios y roles. Solo ADMINISTRADOR")
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
 private final UsuarioAdministracionService servicio;
 @Operation(summary = "Listar usuarios",
         description = "Lista los usuarios del sistema",
         security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"))
 @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Usuarios entregados"),
         @ApiResponse(responseCode = "401", description = "Falta el token"),
         @ApiResponse(responseCode = "403", description = "El usuario no es ADMINISTRADOR")
 })
 @GetMapping public List<UsuarioAdministracionService.UsuarioResponse> listar() { return servicio.listar(); }
 @Operation(summary = "Detalle de usuario",
         description = "Entrega un usuario por id, sin su contraseña",
         security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"))
 @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
         @ApiResponse(responseCode = "401", description = "Falta el token"),
         @ApiResponse(responseCode = "403", description = "El usuario no es ADMINISTRADOR"),
         @ApiResponse(responseCode = "404", description = "No existe el usuario")
 })
 @GetMapping("/{id}") public UsuarioAdministracionService.UsuarioResponse buscar(@PathVariable Long id) { return servicio.buscar(id); }
 @Operation(summary = "Crear usuario",
         description = "Crea un usuario con el rol indicado (CLIENTE, VENDEDOR o ADMINISTRADOR)",
         security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"))
 @ApiResponses(value = {
         @ApiResponse(responseCode = "201", description = "Usuario creado"),
         @ApiResponse(responseCode = "400", description = "Datos inválidos"),
         @ApiResponse(responseCode = "401", description = "Falta el token"),
         @ApiResponse(responseCode = "403", description = "El usuario no es ADMINISTRADOR"),
         @ApiResponse(responseCode = "409", description = "Correo o RUN ya registrado")
 })
 @PostMapping @ResponseStatus(HttpStatus.CREATED)
 public UsuarioAdministracionService.UsuarioResponse crear(@Valid @RequestBody UsuarioAdminRequest datos) { return servicio.crear(datos); }
 @Operation(summary = "Editar usuario",
         description = "Reemplaza los datos de un usuario. Si no se envía contraseña, se conserva la actual",
         security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"))
 @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
         @ApiResponse(responseCode = "400", description = "Datos inválidos"),
         @ApiResponse(responseCode = "401", description = "Falta el token"),
         @ApiResponse(responseCode = "403", description = "El usuario no es ADMINISTRADOR"),
         @ApiResponse(responseCode = "404", description = "No existe el usuario")
 })
 @PutMapping("/{id}") public UsuarioAdministracionService.UsuarioResponse editar(@PathVariable Long id,@Valid @RequestBody UsuarioAdminRequest datos,@AuthenticationPrincipal Jwt jwt) { return servicio.editar(id,datos,jwt.getSubject()); }
 @Operation(summary = "Cambiar rol",
         description = "Cambia el rol de un usuario. Un administrador no puede quitarse su propio rol",
         security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"))
 @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Rol actualizado"),
         @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
         @ApiResponse(responseCode = "401", description = "Falta el token"),
         @ApiResponse(responseCode = "403", description = "El usuario no es ADMINISTRADOR"),
         @ApiResponse(responseCode = "404", description = "No existe el usuario")
 })
 @PatchMapping("/{id}/rol") public UsuarioAdministracionService.UsuarioResponse rol(@PathVariable Long id,@Valid @RequestBody CambiarRolRequest datos,@AuthenticationPrincipal Jwt jwt) { return servicio.cambiarRol(id,datos.tipo(),jwt.getSubject()); }
 @Operation(summary = "Desactivar usuario",
         description = "Da de baja a un usuario sin borrar sus boletas. Un administrador no puede desactivarse a sí mismo",
         security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"))
 @ApiResponses(value = {
         @ApiResponse(responseCode = "204", description = "Usuario desactivado"),
         @ApiResponse(responseCode = "401", description = "Falta el token"),
         @ApiResponse(responseCode = "403", description = "El usuario no es ADMINISTRADOR"),
         @ApiResponse(responseCode = "404", description = "No existe el usuario")
 })
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
 public void desactivar(@PathVariable Long id,@AuthenticationPrincipal Jwt jwt) { servicio.desactivar(id,jwt.getSubject()); }
 public record CambiarRolRequest(@NotNull RolUsuario tipo) {}
}
