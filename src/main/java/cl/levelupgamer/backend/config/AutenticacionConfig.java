
package cl.levelupgamer.backend.config;

import cl.levelupgamer.backend.auth.UsuarioDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AutenticacionConfig {

    @Bean
    public AuthenticationManager authenticationManager(
            UsuarioDetailsService usuarioDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider proveedor =
                new DaoAuthenticationProvider(usuarioDetailsService);

        proveedor.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(proveedor);
    }
}
