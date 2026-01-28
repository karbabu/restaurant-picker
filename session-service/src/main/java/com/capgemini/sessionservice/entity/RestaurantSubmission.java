package com.capgemini.sessionservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "restaurant_submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String submissionId;

    @Column(nullable = false, length = 200)
    private String restaurantName;

    @Column(length = 500)
    private String address;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String submittedByUserId;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;
}