package com.example.bankcards.dto.response;

import com.example.bankcards.entity.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class CardResponse {
    private UUID id;
    private String maskedNumber;
    private String holder;
    private LocalDate expirationDate;
    private CardStatus status;
    private BigDecimal balance;
}
