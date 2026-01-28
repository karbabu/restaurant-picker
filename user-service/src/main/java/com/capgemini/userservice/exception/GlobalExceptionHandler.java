package com.capgemini.userservice.exception;

import com.capgemini.common.dto.ErrorResponseDTO;
import com.capgemini.common.exception.BusinessException;
import com.capgemini.common.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String SERVICE_NAME = "user-service";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {

        String traceId = generateTraceId();
        log.error("[{}] Resource not found: {}", traceId, ex.getMessage());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
               // .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
              //  .path(request.getDescription(false).replace("uri=", ""))
              //  .service(SERVICE_NAME)
              //  .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessException(
            BusinessException ex, WebRequest request) {

        String traceId = generateTraceId();
        log.error("[{}] Business error: {}", traceId, ex.getMessage());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
              //  .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Business Error")
                .message(ex.getMessage())
               // .path(request.getDescription(false).replace("uri=", ""))
              //  .service(SERVICE_NAME)
              //  .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {

        String traceId = generateTraceId();
        log.error("[{}] Validation error: {}", traceId, ex.getMessage());

        List<ErrorResponseDTO.ValidationError> validationErrors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            Object rejectedValue = ((FieldError) error).getRejectedValue();
            validationErrors.add(new ErrorResponseDTO.ValidationError(
                    fieldName, errorMessage, rejectedValue
            ));
        });

        ErrorResponseDTO error = ErrorResponseDTO.builder()
              //  .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Input validation failed")
               // .path(request.getDescription(false).replace("uri=", ""))
               // .service(SERVICE_NAME)
               // .traceId(traceId)
                //.validationErrors(validationErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex, WebRequest request) {

        String traceId = generateTraceId();
        log.error("[{}] Unexpected error: ", traceId, ex);

        ErrorResponseDTO error = ErrorResponseDTO.builder()
              //  .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please contact support with trace ID: " + traceId)
               // .path(request.getDescription(false).replace("uri=", ""))
               // .service(SERVICE_NAME)
               // .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}