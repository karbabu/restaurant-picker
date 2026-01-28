package com.capgemini.userservice.service;

import com.capgemini.common.dto.UserDTO;
import com.capgemini.common.exception.ResourceNotFoundException;
import com.capgemini.userservice.entity.User;
import com.capgemini.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->  new ResourceNotFoundException("User not found: ", userId));//new RuntimeException("User not found: " + userId));
        return convertToDTO(user);
    }

    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: ", username));//new RuntimeException("User not found: " + username));
        return convertToDTO(user);
    }

    public boolean canUserInitiateSession(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: ", userId));//new RuntimeException("User not found: " + userId));
        return user.isCanInitiateSession();
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.isCanInitiateSession()
        );
    }
}