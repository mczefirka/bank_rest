package com.example.bankcards.dto.request;

import com.example.bankcards.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateUserRequest {
    @NotBlank(message = "Name may not be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Surname may not be blank")
    @Size(min = 2, max = 100, message = "Surname must be between 2 and 100 characters")
    private String surname;

    @NotNull(message = "Birth date may not be null")
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @NotBlank(message = "Email may not be blank")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password may not be blank")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @NotNull(message = "Role may not be null")
    private UserRole role;
}
