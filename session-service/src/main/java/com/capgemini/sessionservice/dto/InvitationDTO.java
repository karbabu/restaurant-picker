package com.capgemini.sessionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvitationDTO {
    private String sessionId;
    private List<String> invitedUserIds;
}