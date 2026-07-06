package com.example.bankcards.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(BigDecimal balance, BigDecimal required) {
        super("Insufficient funds. Current balance: " + balance + ", required: " + required);
    }
}
