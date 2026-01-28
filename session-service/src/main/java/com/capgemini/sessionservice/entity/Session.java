package com.capgemini.sessionservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String sessionId;

    @Column(nullable = false)
    private String initiatorUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.ACTIVE;

    @ElementCollection
    @CollectionTable(name = "session_participants",
            joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "participant_id")
    private List<String> participantIds = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RestaurantSubmission> submissions = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "selected_restaurant_id")
    private RestaurantSubmission selectedRestaurant;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime endedAt;

    public enum SessionStatus {
        ACTIVE, ENDED
    }
}