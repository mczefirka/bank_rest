package com.example.bankcards.service;

import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.request.UpdateCardRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.BalanceResponse;
import com.example.bankcards.dto.response.PageResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface CardService {
    CardResponse createCard(CreateCardRequest request);
    CardResponse getCard(UUID id, UUID userId);
    CardResponse getCardAdmin(UUID id);
    PageResponse<CardResponse> getUserCards(UUID userId, int page, int size,
                                              String status, String holder, LocalDate expirationDate);
    PageResponse<CardResponse> getAllCards(int page, int size, String status,
                                             String holder, LocalDate expirationDate);
    CardResponse updateCard(UUID id, UpdateCardRequest request);
    void deleteCard(UUID id);
    void blockCard(UUID id);
    void activateCard(UUID id);
    void blockOwnCard(UUID id, UUID userId);
    void transfer(TransferRequest request, UUID userId);
    BalanceResponse getBalance(UUID id, UUID userId);
}
