
package cl.levelupgamer.backend.usuarios;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RegistroController {

    private final UsuarioService usuarioService;

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
