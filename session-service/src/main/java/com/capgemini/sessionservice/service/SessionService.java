package com.capgemini.sessionservice.service;

import com.capgemini.common.dto.SessionDTO;
import com.capgemini.common.dto.RestaurantSubmissionDTO;
import com.capgemini.common.dto.UserDTO;
import com.capgemini.sessionservice.dto.NotificationDTO;
import com.capgemini.sessionservice.entity.RestaurantSubmission;
import com.capgemini.sessionservice.entity.Session;
import com.capgemini.sessionservice.repository.RestaurantSubmissionRepository;
import com.capgemini.sessionservice.repository.SessionRepository;
import com.capgemini.sessionservice.client.UserServiceClient;
import com.capgemini.common.exception.ResourceNotFoundException;
import com.capgemini.common.exception.UnauthorizedException;
import com.capgemini.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;
    private final RestaurantSubmissionRepository submissionRepository;
    private final UserServiceClient userServiceClient;
    private final NotificationService notificationService;
    private final Random random = new Random();

    @Transactional
    public SessionDTO createSession(String initiatorUserId, List<String> invitedUserIds) {
        log.info("Creating session - Initiator: {}, Invited: {}", initiatorUserId, invitedUserIds);

        boolean canInitiate = userServiceClient.canUserInitiateSession(initiatorUserId);
        if (!canInitiate) {
            throw new UnauthorizedException("User is not authorized to initiate a session");
        }

        UserDTO initiator = userServiceClient.getUserById(initiatorUserId);

        Session session = new Session();
        session.setInitiatorUserId(initiatorUserId);
        session.setStatus(Session.SessionStatus.ACTIVE);
        session.getParticipantIds().add(initiatorUserId);
        session.setCreatedAt(LocalDateTime.now());

        session = sessionRepository.save(session);
        log.info("Session created: {}", session.getSessionId());

        final String sessionId = session.getSessionId();
        final String initiatorUsername = initiator.getUsername();

        if (invitedUserIds != null && !invitedUserIds.isEmpty()) {
            invitedUserIds.forEach(invitedUserId -> {
                NotificationDTO invitation = NotificationDTO.personalInvitation(
                        invitedUserId,
                        sessionId,
                        initiatorUsername
                );
                notificationService.sendPersonalNotification(invitedUserId, invitation);
            });
        }

        NotificationDTO globalNotif = NotificationDTO.sessionCreated(
                sessionId,
                initiatorUsername
        );
        notificationService.sendGlobalNotification(globalNotif);

        return convertToDTO(session);
    }

    @Transactional
    public SessionDTO joinSession(String sessionId, String userId) {
        log.info("User {} joining session {}", userId, sessionId);

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", sessionId));

        if (session.getStatus() == Session.SessionStatus.ENDED) {
            throw new BusinessException("SESSION_ENDED", "Cannot join a session that has already ended");
        }

        if (!session.getParticipantIds().contains(userId)) {
            session.getParticipantIds().add(userId);
            session = sessionRepository.save(session);

            UserDTO user = userServiceClient.getUserById(userId);

            final String finalSessionId = session.getSessionId();
            final String username = user.getUsername();
            final int participantCount = session.getParticipantIds().size();

            NotificationDTO sessionNotif = NotificationDTO.userJoined(
                    finalSessionId,
                    username,
                    participantCount
            );
            notificationService.sendSessionNotification(finalSessionId, sessionNotif);

            log.info("User {} joined session {} ({} participants)",
                    userId, finalSessionId, participantCount);
        }

        return convertToDTO(session);
    }

    @Transactional
    public RestaurantSubmissionDTO submitRestaurant(String sessionId, RestaurantSubmissionDTO submissionDTO) {
        log.info("Submitting restaurant to session {}: {}", sessionId, submissionDTO.getRestaurantName());

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", sessionId));

        if (session.getStatus() == Session.SessionStatus.ENDED) {
            throw new BusinessException("SESSION_ENDED", "Cannot submit to an ended session");
        }

        if (!session.getParticipantIds().contains(submissionDTO.getSubmittedByUserId())) {
            throw new UnauthorizedException("User must join the session before submitting");
        }

        RestaurantSubmission submission = new RestaurantSubmission();
        submission.setRestaurantName(sanitizeInput(submissionDTO.getRestaurantName()));
        submission.setAddress(sanitizeInput(submissionDTO.getAddress()));
        submission.setDescription(sanitizeInput(submissionDTO.getDescription()));
        submission.setSubmittedByUserId(submissionDTO.getSubmittedByUserId());
        submission.setSession(session);

        submission = submissionRepository.save(submission);

        UserDTO user = userServiceClient.getUserById(submissionDTO.getSubmittedByUserId());

        final String finalSessionId = session.getSessionId();
        final String username = user.getUsername();
        final String restaurantName = submission.getRestaurantName();
        final int totalSubmissions = session.getSubmissions().size() + 1;

        NotificationDTO sessionNotif = NotificationDTO.restaurantSubmitted(
                finalSessionId,
                username,
                restaurantName,
                totalSubmissions
        );
        notificationService.sendSessionNotification(finalSessionId, sessionNotif);

        log.info("Restaurant submitted: {} for session {} (Total: {})",
                restaurantName, finalSessionId, totalSubmissions);

        return convertSubmissionToDTO(submission);
    }

    @Transactional
    public SessionDTO endSession(String sessionId, String userId) {
        log.info("Ending session {} by user {}", sessionId, userId);

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", sessionId));

        if (!session.getInitiatorUserId().equals(userId)) {
            throw new UnauthorizedException("Only the session initiator can end the session");
        }

        if (session.getStatus() == Session.SessionStatus.ENDED) {
            throw new BusinessException("SESSION_ALREADY_ENDED", "Session is already ended");
        }

        List<RestaurantSubmission> submissions = session.getSubmissions();
        if (submissions.isEmpty()) {
            throw new BusinessException("NO_SUBMISSIONS", "No restaurants have been submitted");
        }

        RestaurantSubmission selected = submissions.get(random.nextInt(submissions.size()));
        session.setSelectedRestaurant(selected);
        session.setStatus(Session.SessionStatus.ENDED);
        session.setEndedAt(LocalDateTime.now());

        session = sessionRepository.save(session);

        final String finalSessionId = session.getSessionId();
        final String restaurantName = selected.getRestaurantName();
        final String address = selected.getAddress();
        final List<String> participantIds = new ArrayList<>(session.getParticipantIds());

        NotificationDTO sessionEndNotif = NotificationDTO.sessionEnded(
                finalSessionId,
                restaurantName,
                address
        );
        notificationService.sendSessionNotification(finalSessionId, sessionEndNotif);

        participantIds.forEach(participantId -> {
            NotificationDTO personalNotif = NotificationDTO.builder()
                    .type(NotificationDTO.NotificationType.SESSION_ENDED)
                    .sessionId(finalSessionId)
                    .userId(participantId)
                    .title("Lunch Decided!")
                    .message("Your group is going to " + restaurantName)
                    .priority(NotificationDTO.NotificationPriority.HIGH)
                    .timestamp(LocalDateTime.now())
                    .data(java.util.Map.of(
                            "restaurantName", restaurantName,
                            "address", address != null ? address : "N/A",
                            "sessionId", finalSessionId
                    ))
                    .build();

            notificationService.sendPersonalNotification(participantId, personalNotif);
        });

        log.info("Session {} ended. Selected restaurant: {}", finalSessionId, restaurantName);

        return convertToDTO(session);
    }

    public SessionDTO getSession(String sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", sessionId));
        return convertToDTO(session);
    }

    public List<SessionDTO> getAllActiveSessions() {
        return sessionRepository.findByStatus(Session.SessionStatus.ACTIVE).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<RestaurantSubmissionDTO> getSessionSubmissions(String sessionId) {
        return submissionRepository.findBySession_SessionId(sessionId).stream()
                .map(this::convertSubmissionToDTO)
                .collect(Collectors.toList());
    }

    private String sanitizeInput(String input) {
        if (input == null) return null;
        return input.replaceAll("[<>\"']", "").trim();
    }

    private SessionDTO convertToDTO(Session session) {
        List<RestaurantSubmissionDTO> submissionDTOs = session.getSubmissions().stream()
                .map(this::convertSubmissionToDTO)
                .collect(Collectors.toList());

        return new SessionDTO(
                session.getSessionId(),
                session.getInitiatorUserId(),
                session.getStatus().name(),
                session.getParticipantIds(),
                submissionDTOs,
                session.getSelectedRestaurant() != null ?
                        convertSubmissionToDTO(session.getSelectedRestaurant()) : null,
                session.getCreatedAt(),
                session.getEndedAt()
        );
    }

    private RestaurantSubmissionDTO convertSubmissionToDTO(RestaurantSubmission submission) {
        return new RestaurantSubmissionDTO(
                submission.getSubmissionId(),
                submission.getRestaurantName(),
                submission.getAddress(),
                submission.getDescription(),
                submission.getSubmittedByUserId(),
                submission.getSession().getSessionId()
        );
    }
}