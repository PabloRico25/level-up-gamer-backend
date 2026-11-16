package cl.levelupgamer.backend.usuarios;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class RegistroRequest {

    @NotBlank
    @Size(max = 15)
    private String run;

    @NotBlank
    @Size(max = 50)
    private String nombre;

    @NotBlank
    @Size(max = 100)
    private String apellidos;

    @NotBlank
    @Pattern(regexp = "(?i)^[^@\\s]+@(duoc\\.cl|profesor\\.duoc\\.cl|gmail\\.com)$", message="Usa un correo Duoc, profesor Duoc o Gmail")
    @Email
    @Size(max = 100)
    private String correo;

    @NotBlank
    @Size(min = 8, max = 72)
    private String contrasena;

    @NotNull
    @Past
    private LocalDate fechaNacimiento;

    @NotBlank
    @Size(max = 255)
    private String region;

    @NotBlank
    @Size(max = 255)
    private String comuna;

    @NotBlank
    @Size(max = 300)
    private String direccion;
}