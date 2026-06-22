package com.example.bankcards.exception;

import java.util.UUID;

public class UnauthorizedCardAccessException extends RuntimeException {
    public UnauthorizedCardAccessException(UUID cardId) {
        super("User does not have access to card: " + cardId);
    }
}
