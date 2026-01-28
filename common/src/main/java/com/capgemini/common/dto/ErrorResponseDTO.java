package com.capgemini.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseDTO {
  /*  private LocalDateTime timestamp;*/
    private int status;
    private String error;
    private String message;
   /* private String path;
    private String service;
    private String traceId;  // For distributed tracing
    private List<ValidationError> validationErrors;
    private Map<String, Object> additionalInfo;
*/
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
        private Object rejectedValue;
    }

    public static ErrorResponseDTO of(int status, String error, String message, String path, String service) {
        return ErrorResponseDTO.builder()
                //.timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
              //  .path(path)
              //  .service(service)
                .build();
    }
}