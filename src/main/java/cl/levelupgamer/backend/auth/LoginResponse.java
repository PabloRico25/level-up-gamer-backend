package cl.levelupgamer.backend.auth;

import cl.levelupgamer.backend.usuarios.RolUsuario;

public record LoginResponse(
        String accessToken,
        String tokenType,
        UsuarioResumen usuario
) {

    public record UsuarioResumen(
            Long id,
            String nombre,
            String correo,
            RolUsuario tipo
    ) {
    }
}
