package com.example.bankcards.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class BalanceResponse {
    private UUID cardId;
    private String maskedNumber;
    private BigDecimal balance;
}
