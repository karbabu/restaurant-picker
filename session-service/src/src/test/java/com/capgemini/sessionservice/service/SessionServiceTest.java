package com.capgemini.sessionservice.service;

import com.capgemini.common.dto.SessionDTO;
import com.capgemini.sessionservice.client.UserServiceClient;
import com.capgemini.sessionservice.entity.Session;
import com.capgemini.sessionservice.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private SessionService sessionService;

    private String testUserId;

    @BeforeEach
    void setUp() {
        testUserId = "test-user-123";
    }

    @Test
    void createSession_WhenUserCanInitiate_ShouldCreateSession() {
        // Arrange
        when(userServiceClient.canUserInitiateSession(testUserId)).thenReturn(true);
        Session session = new Session();
        session.setSessionId("session-123");
        session.setInitiatorUserId(testUserId);
        when(sessionRepository.save(any(Session.class))).thenReturn(session);

        // Act
        SessionDTO result = sessionService.createSession(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(testUserId, result.getInitiatorUserId());
        assertEquals("ACTIVE", result.getStatus());
        verify(sessionRepository, times(1)).save(any(Session.class));
    }

    @Test
    void createSession_WhenUserCannotInitiate_ShouldThrowException() {
        // Arrange
        when(userServiceClient.canUserInitiateSession(testUserId)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> sessionService.createSession(testUserId));
        verify(sessionRepository, never()).save(any(Session.class));
    }
}