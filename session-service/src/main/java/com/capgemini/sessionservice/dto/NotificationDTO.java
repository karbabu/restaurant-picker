package com.capgemini.sessionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private String id;
    private NotificationType type;
    private String channel;  // Which channel it was sent to
    private String sessionId;
    private String userId;
    private String title;
    private String message;
    private Map<String, Object> data;
    private NotificationPriority priority;
    private LocalDateTime timestamp;

    public enum NotificationType {
        // Personal notifications
        INVITATION_RECEIVED,
        MENTIONED_IN_SESSION,

        // Session-specific notifications
        SESSION_CREATED,
        USER_JOINED,
        USER_LEFT,
        RESTAURANT_SUBMITTED,
        RESTAURANT_UPDATED,
        RESTAURANT_DELETED,
        SESSION_ENDING_SOON,
        SESSION_ENDED,

        // Global notifications
        SYSTEM_ANNOUNCEMENT,
        MAINTENANCE_SCHEDULED,
        NEW_FEATURE
    }

    public enum NotificationPriority {
        LOW, NORMAL, HIGH, URGENT
    }

    // Factory methods for different notification types

    public static NotificationDTO personalInvitation(String userId, String sessionId, String inviterName) {
        return NotificationDTO.builder()
                .id(generateId())
                .type(NotificationType.INVITATION_RECEIVED)
                .channel("/user/" + userId + "/queue/notifications")
                .sessionId(sessionId)
                .userId(userId)
                .title("Session Invitation")
                .message(inviterName + " invited you to join a lunch session")
                .priority(NotificationPriority.HIGH)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "inviterName", inviterName,
                    "sessionId", sessionId,
                    "action", "join"
                ))
                .build();
    }

    public static NotificationDTO sessionCreated(String sessionId, String initiatorName) {
        return NotificationDTO.builder()
                .id(generateId())
                .type(NotificationType.SESSION_CREATED)
                .channel("/topic/global")
                .sessionId(sessionId)
                .title("New Session Available")
                .message(initiatorName + " created a new lunch session")
                .priority(NotificationPriority.NORMAL)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "sessionId", sessionId,
                    "initiatorName", initiatorName
                ))
                .build();
    }

    public static NotificationDTO userJoined(String sessionId, String username, int totalParticipants) {
        return NotificationDTO.builder()
                .id(generateId())
                .type(NotificationType.USER_JOINED)
                .channel("/topic/session/" + sessionId)
                .sessionId(sessionId)
                .title("User Joined")
                .message(username + " joined the session (" + totalParticipants + " participants)")
                .priority(NotificationPriority.NORMAL)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "username", username,
                    "totalParticipants", totalParticipants
                ))
                .build();
    }

    public static NotificationDTO restaurantSubmitted(String sessionId, String username, String restaurantName, int totalSubmissions) {
        return NotificationDTO.builder()
                .id(generateId())
                .type(NotificationType.RESTAURANT_SUBMITTED)
                .channel("/topic/session/" + sessionId)
                .sessionId(sessionId)
                .title("Restaurant Suggested")
                .message(username + " suggested " + restaurantName + " (" + totalSubmissions + " total)")
                .priority(NotificationPriority.NORMAL)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "username", username,
                    "restaurantName", restaurantName,
                    "totalSubmissions", totalSubmissions
                ))
                .build();
    }

    public static NotificationDTO sessionEnded(String sessionId, String selectedRestaurant, String address) {
        return NotificationDTO.builder()
                .id(generateId())
                .type(NotificationType.SESSION_ENDED)
                .channel("/topic/session/" + sessionId)
                .sessionId(sessionId)
                .title("Session Ended!")
                .message("Selected restaurant: " + selectedRestaurant)
                .priority(NotificationPriority.HIGH)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                    "restaurantName", selectedRestaurant,
                    "address", address != null ? address : "N/A"
                ))
                .build();
    }

    public static NotificationDTO systemAnnouncement(String title, String message) {
        return NotificationDTO.builder()
                .id(generateId())
                .type(NotificationType.SYSTEM_ANNOUNCEMENT)
                .channel("/topic/global")
                .title(title)
                .message(message)
                .priority(NotificationPriority.NORMAL)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private static String generateId() {
        return java.util.UUID.randomUUID().toString().substring(0, 8);
    }
}