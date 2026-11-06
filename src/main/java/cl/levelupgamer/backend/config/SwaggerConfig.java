package cl.levelupgamer.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI levelUpGamerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LEVEL-UP GAMER API")
                        .version("v1")
                        .description("API REST de la tienda LEVEL-UP GAMER (Evaluación Parcial 3, DSY1104)."))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}