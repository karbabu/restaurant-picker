package com.capgemini.sessionservice.service;

import com.capgemini.sessionservice.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * CHANNEL 1: Personal Notifications
     * Send notification to a specific user's personal queue
     * Destination: /user/{userId}/queue/notifications
     */
    public void sendPersonalNotification(String userId, NotificationDTO notification) {
        notification.setChannel("/user/" + userId + "/queue/notifications");
        log.info("[PERSONAL] Sending to user {}: {} - {}",
            userId, notification.getType(), notification.getMessage());

        messagingTemplate.convertAndSendToUser(
            userId,
            "/queue/notifications",
            notification
        );
    }

    /**
     * CHANNEL 1: Personal Notifications (Multiple Users)
     * Send same notification to multiple users
     */
    public void sendPersonalNotifications(List<String> userIds, NotificationDTO notification) {
        log.info("[PERSONAL] Broadcasting to {} users: {}",
            userIds.size(), notification.getMessage());

        userIds.forEach(userId -> sendPersonalNotification(userId, notification));
    }

    /**
     * CHANNEL 2: Session-Specific Notifications
     * Broadcast to all users subscribed to a specific session
     * Destination: /topic/session/{sessionId}
     */
    public void sendSessionNotification(String sessionId, NotificationDTO notification) {
        notification.setChannel("/topic/session/" + sessionId);
        log.info("[SESSION:{}] Broadcasting: {} - {}",
            sessionId, notification.getType(), notification.getMessage());

        messagingTemplate.convertAndSend(
            "/topic/session/" + sessionId,
            notification
        );
    }

    /**
     * CHANNEL 3: Global System Notifications
     * Broadcast to all connected users
     * Destination: /topic/global
     */
    public void sendGlobalNotification(NotificationDTO notification) {
        notification.setChannel("/topic/global");
        log.info("[GLOBAL] Broadcasting: {} - {}",
            notification.getType(), notification.getMessage());

        messagingTemplate.convertAndSend(
            "/topic/global",
            notification
        );
    }

    /**
     * Send notification to specific user AND their session
     * Useful when an action affects both
     */
    public void sendDualNotification(String userId, String sessionId,
                                     NotificationDTO personalNotif,
                                     NotificationDTO sessionNotif) {
        sendPersonalNotification(userId, personalNotif);
        sendSessionNotification(sessionId, sessionNotif);
    }

    /**
     * Send notification to multiple channels
     */
    public void sendMultiChannelNotification(NotificationDTO notification,
                                            List<String> userIds,
                                            String sessionId,
                                            boolean includeGlobal) {
        // Send to specific users
        if (userIds != null && !userIds.isEmpty()) {
            sendPersonalNotifications(userIds, notification);
        }

        // Send to session
        if (sessionId != null) {
            sendSessionNotification(sessionId, notification);
        }

        // Send globally
        if (includeGlobal) {
            sendGlobalNotification(notification);
        }
    }
}