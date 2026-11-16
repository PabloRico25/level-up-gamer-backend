
package cl.levelupgamer.backend.usuarios;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Tag(name = "Registro", description = "Alta pública de clientes")
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RegistroController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Registrar cliente",
            description = "Crea una cuenta con rol CLIENTE. Exige mayoría de edad, RUN válido y correo Duoc, profesor Duoc o Gmail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Correo o RUN ya registrado")
    })
    @PostMapping("/registro")
    public ResponseEntity<Map<String, Object>> registrar(
            @Valid @RequestBody RegistroRequest datos) {

        Long usuarioId = usuarioService.registrar(datos);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "mensaje", "Usuario registrado correctamente",
                        "usuarioId", usuarioId
                ));
    }
}
