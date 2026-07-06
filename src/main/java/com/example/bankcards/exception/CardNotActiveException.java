package com.example.bankcards.exception;

import java.util.UUID;

public class CardNotActiveException extends RuntimeException {
    public CardNotActiveException(UUID cardId) {
        super("Card " + cardId + " is not active");
    }
}
