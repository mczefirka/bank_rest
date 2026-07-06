package com.example.bankcards.dto.request;

import jakarta.validation.constraints.Future;
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
public class UpdateCardRequest {
    @Size(min = 2, max = 200, message = "Holder name must be between 2 and 200 characters")
    private String holder;

    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;
}
