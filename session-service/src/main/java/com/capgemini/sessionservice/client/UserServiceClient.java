package com.capgemini.sessionservice.client;

import com.capgemini.common.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
//@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${user.service.url:http://localhost:8081}")
    private String userServiceUrl;

    public UserServiceClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }
    public boolean canUserInitiateSession(String userId) {
        try {
            Boolean canInitiate = webClientBuilder.build()
                    .get()
                    .uri(userServiceUrl + "/api/users/{userId}/can-initiate", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            return canInitiate != null && canInitiate;
        } catch (Exception e) {
            log.error("Error checking user permissions: {}", e.getMessage());
            throw new RuntimeException("Unable to verify user permissions", e);
        }

    }



    public UserDTO getUserById(String userId) {
        try {
            // 3. Use .build().get() as requested
            return webClientBuilder.build()
                    .get()
                    .uri(userServiceUrl + "/api/users/{userId}", userId)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, response -> Mono.empty())
                    .bodyToMono(UserDTO.class)
                    .timeout(Duration.ofSeconds(2))
                    .block();
        } catch (Exception e) {
            log.error("Failed to retrieve user {} from User Service: {}", userId, e.getMessage());
            return null;
        }
    }


}