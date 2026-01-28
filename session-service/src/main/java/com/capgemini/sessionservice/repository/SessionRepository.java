package com.capgemini.sessionservice.repository;

import com.capgemini.sessionservice.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, String> {
    List<Session> findByStatus(Session.SessionStatus status);
    List<Session> findByInitiatorUserId(String initiatorUserId);
}