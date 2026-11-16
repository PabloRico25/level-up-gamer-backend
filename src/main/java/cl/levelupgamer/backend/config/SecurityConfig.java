
package cl.levelupgamer.backend.config;

import cl.levelupgamer.backend.usuarios.Usuario;
import cl.levelupgamer.backend.usuarios.UsuarioRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
public class SecurityConfig {

    // Obtener los permisos actuales desde MySQL
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(
            UsuarioRepository usuarioRepository
    ) {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            // Buscar al usuario que pertenece al JWT
            Usuario usuario = usuarioRepository
                    .findByCorreo(jwt.getSubject())
                    .orElseThrow(() -> new BadCredentialsException(
                            "Usuario no encontrado"
                    ));

            if (!usuario.isActivo()) throw new BadCredentialsException("Usuario inactivo");

            // Comprobar que el ID coincida
            Object idToken = jwt.getClaims().get("usuarioId");

            if (idToken == null ||
                    !usuario.getId().toString().equals(idToken.toString())) {

                throw new BadCredentialsException(
                        "Token no corresponde al usuario"
                );
            }

            // Obtener el rol ACTUAL desde MySQL
            String rolActual = usuario.getTipo().name();

            return List.<GrantedAuthority>of(
                    new SimpleGrantedAuthority("ROLE_" + rolActual)
            );
        });

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtConverter,
            ErroresSeguridad erroresSeguridad,
            org.springframework.web.cors.UrlBasedCorsConfigurationSource origenes
    ) throws Exception {

        http
                .addFilterBefore(new FiltroOrigen(origenes, erroresSeguridad), org.springframework.web.filter.CorsFilter.class)
                .cors(cors -> {})

                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        // Registro y autenticación
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/registro",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout"
                        ).permitAll()

                        // Productos públicos
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

                        // Administración de usuarios
                        .requestMatchers(
                                "/api/v1/usuarios",
                                "/api/v1/usuarios/**"
                        ).hasRole("ADMINISTRADOR")

                        // Modificación de productos
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
                                "VENDEDOR", "CLIENTE"
                        )

                        // Registrar compras
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/boletas"
                        ).hasAnyRole(
                                "CLIENTE",
                                "ADMINISTRADOR"
                        )

                        .anyRequest().authenticated()
                )

                .exceptionHandling(errores -> errores
                    .authenticationEntryPoint(erroresSeguridad)
                    .accessDeniedHandler(erroresSeguridad))
                .oauth2ResourceServer(oauth2 ->
                        oauth2.authenticationEntryPoint(erroresSeguridad).accessDeniedHandler(erroresSeguridad).jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtConverter)
                        )
                );

        return http.build();
    }
}
