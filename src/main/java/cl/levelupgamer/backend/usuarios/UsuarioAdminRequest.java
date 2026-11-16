package cl.levelupgamer.backend.usuarios;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record UsuarioAdminRequest(
 @NotBlank @Size(max=15) String run,
 @NotBlank @Size(max=50) String nombre,
 @NotBlank @Size(max=100) String apellidos,
 @NotBlank @Email @Size(max=100) @Pattern(regexp="(?i)^[^@\\s]+@(duoc\\.cl|profesor\\.duoc\\.cl|gmail\\.com)$") String correo,
 @Size(min=8,max=72) String contrasena,
 @NotNull @Past LocalDate fechaNacimiento,
 @NotBlank @Size(max=255) String region,
 @NotBlank @Size(max=255) String comuna,
 @NotBlank @Size(max=300) String direccion,
 @NotNull RolUsuario tipo) {}
