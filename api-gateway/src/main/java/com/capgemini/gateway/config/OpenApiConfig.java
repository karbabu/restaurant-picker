package com.capgemini.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gatewayAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Restaurant Picker API Gateway")
                        .description("Unified API for Restaurant Picker Microservices")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Restaurant Picker Team")
                                .email("support@restaurantpicker.com")));
    }
}