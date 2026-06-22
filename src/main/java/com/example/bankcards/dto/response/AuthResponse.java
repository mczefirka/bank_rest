package com.example.bankcards.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class AuthResponse {
    private String token;
    private UUID userId;
    private String email;
    private String role;
}
