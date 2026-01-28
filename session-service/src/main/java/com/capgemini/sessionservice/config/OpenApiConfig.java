package com.capgemini.sessionservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8082}")
    private String serverPort;

    @Bean
    public OpenAPI sessionServiceAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Session Service API")
                        .description("REST API for Session Management in Restaurant Picker Application")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Restaurant Picker Team")
                                .email("support@restaurantpicker.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("API Gateway"),
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Direct Session Service")
                ));
    }
}