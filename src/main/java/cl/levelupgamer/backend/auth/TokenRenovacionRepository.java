
package cl.levelupgamer.backend.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRenovacionRepository
        extends JpaRepository<TokenRenovacion, Long> {

    Optional<TokenRenovacion> findByTokenHash(String tokenHash);

}
