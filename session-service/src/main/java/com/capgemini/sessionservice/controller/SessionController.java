
package com.capgemini.sessionservice.controller;

import com.capgemini.common.dto.SessionDTO;
import com.capgemini.common.dto.RestaurantSubmissionDTO;
//import com.capgemini.sessionservice.dto.InvitationDTO;
import com.capgemini.sessionservice.dto.InvitationDTO;
import com.capgemini.sessionservice.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

        import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Validated
@Tag(name = "Session Management", description = "APIs for managing lunch decision sessions")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    @Operation(summary = "Create new session",
            description = "Initiate a new lunch session and invite other users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Session created successfully"),
            @ApiResponse(responseCode = "403", description = "User not authorized to create sessions"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    /*public ResponseEntity<SessionDTO> createSession(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Session creation request with invitee list")
            @RequestBody InvitationDTO request) */
    public ResponseEntity<SessionDTO> createSession(@RequestBody InvitationDTO request) {
            //(@RequestBody Map<String, String> request) {

      /*  SessionDTO session = sessionService.createSession(
                request.getInvitedUserIds().get(0),
                request.getInvitedUserIds().subList(1, request.getInvitedUserIds().size())
        );*/
       // String userId = request.get("userId");
        //to do only to allow only user who are allowed to create. Else show proper message.
        //At present shows 500 internal error.
        //java.lang.RuntimeException: User is not authorized to initiate a session
        //SessionDTO session = sessionService.createSession(userId);

        SessionDTO session = sessionService.createSession(
                request.getInvitedUserIds().get(0),  // First user is initiator
                request.getInvitedUserIds().subList(1, request.getInvitedUserIds().size())  // Rest are invited
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    @PostMapping("/{sessionId}/join")
    @Operation(summary = "Join session", description = "Join an active lunch session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully joined session"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "400", description = "Cannot join ended session")
    })
    public ResponseEntity<SessionDTO> joinSession(
            @Parameter(description = "ID of the session to join")
            @PathVariable String sessionId,
            @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        SessionDTO session = sessionService.joinSession(sessionId, userId);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/{sessionId}/submit")
    @Operation(summary = "Submit restaurant",
            description = "Submit a restaurant suggestion for the session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Restaurant submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid submission or session ended"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<RestaurantSubmissionDTO> submitRestaurant(
            @Parameter(description = "ID of the session")
            @PathVariable String sessionId,
            @Valid @RequestBody RestaurantSubmissionDTO submission) {
        submission.setSessionId(sessionId);
        RestaurantSubmissionDTO created = sessionService.submitRestaurant(sessionId, submission);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{sessionId}/end")
    @Operation(summary = "End session",
            description = "End the session and randomly select a restaurant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session ended, restaurant selected"),
            @ApiResponse(responseCode = "403", description = "Only initiator can end session"),
            @ApiResponse(responseCode = "400", description = "No restaurants submitted or session already ended")
    })
    public ResponseEntity<SessionDTO> endSession(
            @Parameter(description = "ID of the session to end")
            @PathVariable String sessionId,
            @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        SessionDTO session = sessionService.endSession(sessionId, userId);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Get session details", description = "Retrieve details of a specific session")
    public ResponseEntity<SessionDTO> getSession(
            @Parameter(description = "ID of the session")
            @PathVariable String sessionId) {
        SessionDTO session = sessionService.getSession(sessionId);
        return ResponseEntity.ok(session);
    }

    @GetMapping
    @Operation(summary = "Get active sessions", description = "Retrieve all currently active sessions")
    public ResponseEntity<List<SessionDTO>> getAllActiveSessions() {
        List<SessionDTO> sessions = sessionService.getAllActiveSessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/{sessionId}/submissions")
    @Operation(summary = "Get session submissions",
            description = "Retrieve all restaurant submissions for a session")
    public ResponseEntity<List<RestaurantSubmissionDTO>> getSessionSubmissions(
            @Parameter(description = "ID of the session")
            @PathVariable String sessionId) {
        List<RestaurantSubmissionDTO> submissions = sessionService.getSessionSubmissions(sessionId);
        return ResponseEntity.ok(submissions);
    }
}