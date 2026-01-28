package com.capgemini.userservice.batch;

import lombok.Data;

@Data
public class UserCSV {
    private String username;
    private String email;
    private boolean canInitiateSession;
}