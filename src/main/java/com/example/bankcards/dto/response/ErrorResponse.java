package com.example.bankcards.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class ErrorResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;
    private List<String> errors;

    public ErrorResponse(int status, String message) {
        this(status, message, LocalDateTime.now(), List.of());
    }

    public ErrorResponse(int status, String message, List<String> errors) {
        this(status, message, LocalDateTime.now(), errors);
    }
}
