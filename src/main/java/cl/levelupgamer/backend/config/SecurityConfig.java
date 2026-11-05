
package cl.levelupgamer.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        // Registro y login públicos
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/registro",
                                "/api/v1/auth/login"
                        ).permitAll()

                        // Catálogo público
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/productos",
                                "/api/v1/productos/**",
                                "/api/v1/categorias",
                                "/api/v1/categorias/**"
                        ).permitAll()

                        // Documentación Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Gestión de usuarios
                        .requestMatchers(
                                "/api/v1/usuarios",
                                "/api/v1/usuarios/**"
                        ).hasRole("ADMINISTRADOR")

                        // Modificaciones de productos y categorías
                        .requestMatchers(
                                "/api/v1/productos",
                                "/api/v1/productos/**",
                                "/api/v1/categorias",
                                "/api/v1/categorias/**"
                        ).hasRole("ADMINISTRADOR")

                        // Consulta de boletas
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/boletas",
                                "/api/v1/boletas/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "VENDEDOR"
                        )

                        // Registrar compras
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/boletas"
                        ).hasAnyRole(
                                "CLIENTE",
                                "ADMINISTRADOR"
                        )

                        // Cualquier otra ruta necesita autenticación
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
