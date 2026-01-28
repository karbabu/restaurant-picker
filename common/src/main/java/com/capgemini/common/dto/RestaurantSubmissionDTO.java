package com.capgemini.common.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantSubmissionDTO {
    private String submissionId;

    @NotBlank(message = "Restaurant name is required")
    @Size(max = 200, message = "Restaurant name must not exceed 200 characters")
    private String restaurantName;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private String submittedByUserId;
    private String sessionId;
}