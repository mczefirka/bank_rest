package com.example.bankcards.controller;

import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.request.UpdateCardRequest;
import com.example.bankcards.dto.response.BalanceResponse;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/cards")
    public ResponseEntity<PageResponse<CardResponse>> getUserCards(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String holder,
            @RequestParam(required = false) LocalDate expirationDate) {
        var userId = (UUID) auth.getPrincipal();
        var result = cardService.getUserCards(userId, page, size, status, holder, expirationDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/cards/{id}")
    public ResponseEntity<CardResponse> getCard(@PathVariable UUID id, Authentication auth) {
        var userId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(cardService.getCard(id, userId));
    }

    @GetMapping("/cards/{id}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable UUID id, Authentication auth) {
        var userId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(cardService.getBalance(id, userId));
    }

    @PostMapping("/cards/transfer")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest request, Authentication auth) {
        var userId = (UUID) auth.getPrincipal();
        cardService.transfer(request, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cards/{id}/block")
    public ResponseEntity<Void> blockOwnCard(@PathVariable UUID id, Authentication auth) {
        var userId = (UUID) auth.getPrincipal();
        cardService.blockOwnCard(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/cards")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CreateCardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(request));
    }

    @GetMapping("/admin/cards")
    public ResponseEntity<PageResponse<CardResponse>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String holder,
            @RequestParam(required = false) LocalDate expirationDate) {
        var result = cardService.getAllCards(page, size, status, holder, expirationDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/admin/cards/{id}")
    public ResponseEntity<CardResponse> getCardAdmin(@PathVariable UUID id) {
        return ResponseEntity.ok(cardService.getCardAdmin(id));
    }

    @PutMapping("/admin/cards/{id}")
    public ResponseEntity<CardResponse> updateCard(@PathVariable UUID id,
                                                    @Valid @RequestBody UpdateCardRequest request) {
        return ResponseEntity.ok(cardService.updateCard(id, request));
    }

    @DeleteMapping("/admin/cards/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable UUID id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/admin/cards/{id}/block")
    public ResponseEntity<Void> blockCard(@PathVariable UUID id) {
        cardService.blockCard(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/admin/cards/{id}/activate")
    public ResponseEntity<Void> activateCard(@PathVariable UUID id) {
        cardService.activateCard(id);
        return ResponseEntity.ok().build();
    }
}
