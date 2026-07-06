package com.example.bankcards.exception;

public class DuplicateCardNumberException extends RuntimeException {
    public DuplicateCardNumberException() {
        super("Card with this number already exists");
    }
}
