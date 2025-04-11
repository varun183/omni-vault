package com.omnivault.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("OmniVault API")
                        .version("1.0.0")
                        .description("Personal content management and archiving platform")
                        .contact(new Contact()
                                .name("OmniVault Support")
                                .url("https://omnivault.dev/support")
                                .email("support@omnivault.dev"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT"))
                )
                .servers(List.of(
                        new Server().url("/api").description("Local Development Server"),
                        new Server().url("https://api.omnivault.dev").description("Production Server")
                ))
                .tags(List.of(
                        new Tag().name("Authentication").description("Operations for user authentication and token management"),
                        new Tag().name("Content").description("Operations for managing content items including text notes, links, files, images, and videos"),
                        new Tag().name("Folders").description("Operations for managing folder structure"),
                        new Tag().name("Tags").description("Operations for managing content tags"),
                        new Tag().name("System").description("System-wide operations")
                ));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/auth/**", "/system/**")
                .build();
    }

    @Bean
    public GroupedOpenApi protectedApi() {
        return GroupedOpenApi.builder()
                .group("protected")
                .pathsToMatch("/contents/**", "/folders/**", "/tags/**")
                .build();
    }
}