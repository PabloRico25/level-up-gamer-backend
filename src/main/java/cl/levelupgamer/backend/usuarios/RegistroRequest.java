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
    @Email
    private String correo;

    @NotBlank
    @Size(min = 8)
    private String contrasena;

    @NotNull
    @Past
    private LocalDate fechaNacimiento;

    @NotBlank
    private String region;

    @NotBlank
    private String comuna;

    @NotBlank
    private String direccion;
}