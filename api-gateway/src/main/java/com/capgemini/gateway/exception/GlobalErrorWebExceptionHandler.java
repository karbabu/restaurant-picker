package com.capgemini.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Order(-2)
@Slf4j
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalErrorWebExceptionHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        String traceId = generateTraceId();
        log.error("[{}] Error in API Gateway: ", traceId, ex);

        HttpStatus status = determineHttpStatus(ex);
        Map<String, Object> errorResponse = buildErrorResponse(ex, exchange, status, traceId);

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes = serializeErrorResponse(errorResponse, traceId);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private HttpStatus determineHttpStatus(Throwable ex) {
        // Handle ResponseStatusException
        if (ex instanceof ResponseStatusException) {
            HttpStatusCode statusCode = ((ResponseStatusException) ex).getStatusCode();
            HttpStatus resolved = HttpStatus.resolve(statusCode.value());
            return resolved != null ? resolved : HttpStatus.INTERNAL_SERVER_ERROR;
        }

        // Handle WebClient errors (from downstream services)
        if (ex instanceof WebClientResponseException) {
            HttpStatusCode statusCode = ((WebClientResponseException) ex).getStatusCode();
            HttpStatus resolved = HttpStatus.resolve(statusCode.value());
            return resolved != null ? resolved : HttpStatus.BAD_GATEWAY;
        }

        // Handle common exceptions
        if (ex instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        }

        if (ex instanceof IllegalStateException) {
            return HttpStatus.CONFLICT;
        }

        // Circuit breaker timeout
        if (ex.getMessage() != null && ex.getMessage().contains("TimeLimiter")) {
            return HttpStatus.GATEWAY_TIMEOUT;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private Map<String, Object> buildErrorResponse(
            Throwable ex,
            ServerWebExchange exchange,
            HttpStatus status,
            String traceId) {

        Map<String, Object> errorAttributes = new HashMap<>();

        errorAttributes.put("timestamp", LocalDateTime.now());
        errorAttributes.put("status", status.value());
        errorAttributes.put("error", status.getReasonPhrase());
        errorAttributes.put("message", extractMessage(ex));
        errorAttributes.put("path", exchange.getRequest().getPath().value());
        errorAttributes.put("method", exchange.getRequest().getMethod().name());
        errorAttributes.put("service", "api-gateway");
        errorAttributes.put("traceId", traceId);

        // Add downstream service info if available
        if (ex instanceof WebClientResponseException) {
            WebClientResponseException webEx = (WebClientResponseException) ex;
            errorAttributes.put("downstreamService", extractServiceFromUri(exchange.getRequest().getPath().value()));

            // Try to parse downstream error response
            try {
                String responseBody = webEx.getResponseBodyAsString();
                if (!responseBody.isEmpty()) {
                    Map<String, Object> downstreamError = objectMapper.readValue(responseBody, Map.class);
                    errorAttributes.put("downstreamError", downstreamError);
                }
            } catch (Exception e) {
                log.debug("Could not parse downstream error response", e);
            }
        }

        return errorAttributes;
    }

    private String extractMessage(Throwable ex) {
        if (ex instanceof WebClientResponseException) {
            WebClientResponseException webEx = (WebClientResponseException) ex;

            // Try to get message from response body
            try {
                String body = webEx.getResponseBodyAsString();
                if (!body.isEmpty()) {
                    Map<String, Object> errorBody = objectMapper.readValue(body, Map.class);
                    if (errorBody.containsKey("message")) {
                        return (String) errorBody.get("message");
                    }
                }
            } catch (Exception e) {
                log.debug("Could not parse error message from downstream service", e);
            }

            return "Error communicating with downstream service";
        }

        if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
            return ex.getMessage();
        }

        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            if (rse.getReason() != null) {
                return rse.getReason();
            }
        }

        return "An unexpected error occurred";
    }

    private String extractServiceFromUri(String path) {
        if (path.startsWith("/api/users")) {
            return "user-service";
        } else if (path.startsWith("/api/sessions")) {
            return "session-service";
        }
        return "unknown";
    }

    private byte[] serializeErrorResponse(Map<String, Object> errorResponse, String traceId) {
        try {
            return objectMapper.writeValueAsBytes(errorResponse);
        } catch (JsonProcessingException e) {
            log.error("Error serializing error response", e);
            String fallback = String.format(
                    "{\"timestamp\":\"%s\",\"status\":500,\"error\":\"Internal Server Error\"," +
                            "\"message\":\"Error processing error response\",\"traceId\":\"%s\"}",
                    LocalDateTime.now(), traceId
            );
            return fallback.getBytes(StandardCharsets.UTF_8);
        }
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}