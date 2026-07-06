package com.example.bankcards.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateCardRequest {
    @NotNull(message = "User id may not be null")
    private UUID userId;

    @NotBlank(message = "Card number may not be blank")
    @Size(min = 16, max = 19, message = "Card number must be between 16 and 19 characters")
    private String number;

    @NotBlank(message = "Holder may not be blank")
    @Size(min = 2, max = 200, message = "Holder name must be between 2 and 200 characters")
    private String holder;

    @NotNull(message = "Expiration date may not be null")
    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;
}
