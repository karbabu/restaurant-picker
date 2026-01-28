package com.capgemini.sessionservice.repository;

import com.capgemini.sessionservice.entity.RestaurantSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantSubmissionRepository extends JpaRepository<RestaurantSubmission, String> {
    List<RestaurantSubmission> findBySession_SessionId(String sessionId);
}