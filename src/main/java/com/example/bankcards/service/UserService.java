package com.example.bankcards.service;

import com.example.bankcards.dto.request.CreateUserRequest;
import com.example.bankcards.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse createUser(CreateUserRequest user);
    UserResponse getUser(UUID id);
    List<UserResponse> getUsers();
    UserResponse getUserByEmail(String email);
    void deleteUser(UUID id);
}
