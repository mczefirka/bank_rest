package com.example.bankcards.dto.response;

import com.example.bankcards.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class UserResponse {
    private UUID id;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
    private UserRole role;
}
