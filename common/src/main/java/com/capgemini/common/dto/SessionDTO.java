
package com.capgemini.common.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionDTO {
    private String sessionId;
    private String initiatorUserId;
    private String status; // ACTIVE, ENDED
    private List<String> participantIds;
    private List<RestaurantSubmissionDTO> submissions;
    private RestaurantSubmissionDTO selectedRestaurant;
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;
}