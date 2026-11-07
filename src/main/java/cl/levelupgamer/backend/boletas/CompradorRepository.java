package cl.levelupgamer.backend.boletas;

import cl.levelupgamer.backend.usuarios.Usuario;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface CompradorRepository extends Repository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

}