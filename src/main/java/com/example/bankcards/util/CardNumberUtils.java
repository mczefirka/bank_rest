package com.example.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class CardNumberUtils {

    private final String salt;

    public CardNumberUtils(@Value("${card.number-hash-salt}") String salt) {
        this.salt = salt;
    }

    public String hash(String cardNumber) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            var hashBytes = digest.digest(cardNumber.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash card number", e);
        }
    }

    public String lastFour(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            throw new IllegalArgumentException("Invalid card number");
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }

    public String mask(String lastFour) {
        return "**** **** **** " + lastFour;
    }

    public boolean isValidLuhn(String cardNumber) {
        if (cardNumber == null) {
            return false;
        }
        var digits = cardNumber.replaceAll("\\s", "");
        if (!digits.matches("\\d{16,19}")) {
            return false;
        }
        int sum = 0;
        boolean alternate = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int digit = digits.charAt(i) - '0';
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }
}
