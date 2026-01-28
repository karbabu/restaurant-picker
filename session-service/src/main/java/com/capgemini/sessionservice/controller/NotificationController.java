package com.capgemini.sessionservice.controller;

import com.capgemini.sessionservice.dto.NotificationDTO;
import com.capgemini.sessionservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * REST endpoint to send test personal notification
     */
    @PostMapping("/test/personal/{userId}")
    public ResponseEntity<String> sendTestPersonalNotification(@PathVariable String userId) {
        NotificationDTO notification = NotificationDTO.builder()
                .type(NotificationDTO.NotificationType.SYSTEM_ANNOUNCEMENT)
                .title("Test Personal Notification")
                .message("This is a test personal notification for user " + userId)
                .priority(NotificationDTO.NotificationPriority.NORMAL)
                .timestamp(java.time.LocalDateTime.now())
                .build();

        notificationService.sendPersonalNotification(userId, notification);
        return ResponseEntity.ok("Personal notification sent to user: " + userId);
    }

    /**
     * REST endpoint to send test session notification
     */
    @PostMapping("/test/session/{sessionId}")
    public ResponseEntity<String> sendTestSessionNotification(@PathVariable String sessionId) {
        NotificationDTO notification = NotificationDTO.builder()
                .type(NotificationDTO.NotificationType.SYSTEM_ANNOUNCEMENT)
                .title("Test Session Notification")
                .message("This is a test notification for session " + sessionId)
                .priority(NotificationDTO.NotificationPriority.NORMAL)
                .timestamp(java.time.LocalDateTime.now())
                .build();

        notificationService.sendSessionNotification(sessionId, notification);
        return ResponseEntity.ok("Session notification sent to: " + sessionId);
    }

    /**
     * REST endpoint to send test global notification
     */
    @PostMapping("/test/global")
    public ResponseEntity<String> sendTestGlobalNotification(@RequestBody(required = false) String message) {
        NotificationDTO notification = NotificationDTO.systemAnnouncement(
                "Test Global Announcement",
                message != null ? message : "This is a test global notification"
        );

        notificationService.sendGlobalNotification(notification);
        return ResponseEntity.ok("Global notification broadcasted");
    }

    /**
     * WebSocket message mapping - Client can send messages to server
     * Example: Client sends to /app/notify
     */
    @MessageMapping("/notify")
    @SendTo("/topic/global")
    public NotificationDTO handleClientMessage(@Payload NotificationDTO notification) {
        log.info("Received message from client: {}", notification.getMessage());
        notification.setTimestamp(java.time.LocalDateTime.now());
        return notification;
    }

    /**
     * WebSocket message mapping with user-specific response
     */
    @MessageMapping("/notify/user")
    @SendToUser("/queue/notifications")
    public NotificationDTO handleUserMessage(@Payload NotificationDTO notification, Principal principal) {
        log.info("Received message from user {}: {}", principal.getName(), notification.getMessage());
        notification.setTimestamp(java.time.LocalDateTime.now());
        notification.setUserId(principal.getName());
        return notification;
    }
}