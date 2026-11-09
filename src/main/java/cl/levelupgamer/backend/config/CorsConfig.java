
package cl.levelupgamer.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuracion = new CorsConfiguration();

        // Frontend permitido
        configuracion.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));

        // Métodos HTTP permitidos
        configuracion.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        // Cabeceras permitidas
        configuracion.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With"
        ));

        // Permitir cookies
        configuracion.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource origen =
                new UrlBasedCorsConfigurationSource();

        origen.registerCorsConfiguration(
                "/api/**",
                configuracion
        );

        return origen;
    }
}
