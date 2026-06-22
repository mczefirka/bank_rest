package com.example.bankcards.service;

import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.CardNotActiveException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.DuplicateCardNumberException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.UnauthorizedCardAccessException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.util.CardNumberUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private TransferRepository transferRepository;
    @Mock
    private CardMapper cardMapper;
    @Mock
    private CardNumberUtils cardNumberUtils;

    @Captor
    private ArgumentCaptor<Card> cardCaptor;

    private CardServiceImpl cardService;
    private UUID userId;
    private UUID cardId;
    private User user;
    private Card card;

    @BeforeEach
    void setUp() {
        cardService = new CardServiceImpl(cardRepository, transferRepository, cardMapper, cardNumberUtils);

        userId = UUID.randomUUID();
        cardId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setName("John");
        user.setSurname("Doe");
        user.setRole(UserRole.USER);

        card = new Card();
        card.setId(cardId);
        card.setUser(user);
        card.setNumberHash("hash123");
        card.setLastFour("1234");
        card.setHolder("John Doe");
        card.setExpirationDate(LocalDate.now().plusYears(3));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.valueOf(1000));
        card.setVersion(0L);
    }

    @Test
    void createCard_shouldCreateActiveCardWithZeroBalance() {
        var request = new CreateCardRequest();
        request.setUserId(userId);
        request.setNumber("4532015112890367");
        request.setHolder("John Doe");
        request.setExpirationDate(LocalDate.now().plusYears(3));

        var mappedCard = new Card();
        mappedCard.setUser(user);
        mappedCard.setHolder("John Doe");
        mappedCard.setExpirationDate(LocalDate.now().plusYears(3));

        when(cardNumberUtils.isValidLuhn(anyString())).thenReturn(true);
        when(cardNumberUtils.hash(anyString())).thenReturn("hash123");
        when(cardRepository.existsByNumberHash("hash123")).thenReturn(false);
        when(cardMapper.toEntity(request)).thenReturn(mappedCard);
        when(cardNumberUtils.lastFour(anyString())).thenReturn("0367");
        when(cardNumberUtils.mask("0367")).thenReturn("**** **** **** 0367");
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            var saved = invocation.<Card>getArgument(0);
            saved.setId(cardId);
            return saved;
        });

        var result = cardService.createCard(request);

        assertNotNull(result);
        verify(cardRepository).save(cardCaptor.capture());
        var saved = cardCaptor.getValue();
        assertEquals("hash123", saved.getNumberHash());
        assertEquals("0367", saved.getLastFour());
        assertEquals(CardStatus.ACTIVE, saved.getStatus());
        assertEquals(BigDecimal.ZERO, saved.getBalance());
    }

    @Test
    void createCard_shouldThrowOnDuplicateNumber() {
        var request = new CreateCardRequest();
        request.setNumber("4532015112890367");

        when(cardNumberUtils.isValidLuhn(anyString())).thenReturn(true);
        when(cardNumberUtils.hash(anyString())).thenReturn("hash123");
        when(cardRepository.existsByNumberHash("hash123")).thenReturn(true);

        assertThrows(DuplicateCardNumberException.class, () -> cardService.createCard(request));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void getCard_shouldReturnCardForOwner() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardNumberUtils.mask("1234")).thenReturn("**** **** **** 1234");

        var result = cardService.getCard(cardId, userId);

        assertNotNull(result);
        assertEquals(cardId, result.getId());
        assertEquals("**** **** **** 1234", result.getMaskedNumber());
    }

    @Test
    void getCard_shouldThrowWhenNotFound() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.getCard(cardId, userId));
    }

    @Test
    void getCard_shouldThrowForWrongOwner() {
        var otherUserId = UUID.randomUUID();
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        assertThrows(UnauthorizedCardAccessException.class,
            () -> cardService.getCard(cardId, otherUserId));
    }

    @Test
    void transfer_shouldTransferFunds() {
        var toCardId = UUID.randomUUID();
        var toCard = new Card();
        toCard.setId(toCardId);
        toCard.setUser(user);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setBalance(BigDecimal.valueOf(500));
        toCard.setExpirationDate(LocalDate.now().plusYears(3));

        var request = new TransferRequest();
        request.setFromCardId(cardId);
        request.setToCardId(toCardId);
        request.setAmount(BigDecimal.valueOf(200));

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));

        cardService.transfer(request, userId);

        assertEquals(BigDecimal.valueOf(800), card.getBalance());
        assertEquals(BigDecimal.valueOf(700), toCard.getBalance());
        verify(transferRepository).save(any());
    }

    @Test
    void transfer_shouldThrowWhenInsufficientFunds() {
        var toCardId = UUID.randomUUID();
        var toCard = new Card();
        toCard.setId(toCardId);
        toCard.setUser(user);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setBalance(BigDecimal.valueOf(500));
        toCard.setExpirationDate(LocalDate.now().plusYears(3));

        var request = new TransferRequest();
        request.setFromCardId(cardId);
        request.setToCardId(toCardId);
        request.setAmount(BigDecimal.valueOf(2000));

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));

        assertThrows(InsufficientFundsException.class, () -> cardService.transfer(request, userId));
    }

    @Test
    void transfer_shouldThrowWhenFromCardIsNotActive() {
        card.setStatus(CardStatus.BLOCKED);
        var toCardId = UUID.randomUUID();
        var toCard = new Card();
        toCard.setId(toCardId);
        toCard.setUser(user);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setExpirationDate(LocalDate.now().plusYears(3));

        var request = new TransferRequest();
        request.setFromCardId(cardId);
        request.setToCardId(toCardId);
        request.setAmount(BigDecimal.valueOf(100));

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));

        assertThrows(CardNotActiveException.class, () -> cardService.transfer(request, userId));
    }

    @Test
    void transfer_shouldThrowWhenCardsNotOwnedByUser() {
        var otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        var otherCard = new Card();
        otherCard.setId(UUID.randomUUID());
        otherCard.setUser(otherUser);
        otherCard.setStatus(CardStatus.ACTIVE);
        otherCard.setExpirationDate(LocalDate.now().plusYears(3));

        var request = new TransferRequest();
        request.setFromCardId(cardId);
        request.setToCardId(otherCard.getId());
        request.setAmount(BigDecimal.valueOf(100));

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.findById(otherCard.getId())).thenReturn(Optional.of(otherCard));

        assertThrows(UnauthorizedCardAccessException.class,
            () -> cardService.transfer(request, userId));
    }

    @Test
    void blockOwnCard_shouldBlockCard() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        cardService.blockOwnCard(cardId, userId);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void blockOwnCard_shouldThrowForWrongOwner() {
        var otherUserId = UUID.randomUUID();
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        assertThrows(UnauthorizedCardAccessException.class,
            () -> cardService.blockOwnCard(cardId, otherUserId));
    }

    @Test
    void deleteCard_shouldDeleteExistingCard() {
        when(cardRepository.existsById(cardId)).thenReturn(true);

        cardService.deleteCard(cardId);

        verify(cardRepository).deleteById(cardId);
    }

    @Test
    void deleteCard_shouldThrowWhenNotFound() {
        when(cardRepository.existsById(cardId)).thenReturn(false);

        assertThrows(CardNotFoundException.class, () -> cardService.deleteCard(cardId));
    }

    @Test
    void transfer_shouldThrowWhenSameCard() {
        var request = new TransferRequest();
        request.setFromCardId(cardId);
        request.setToCardId(cardId);
        request.setAmount(BigDecimal.valueOf(100));

        assertThrows(IllegalArgumentException.class, () -> cardService.transfer(request, userId));
    }
}
