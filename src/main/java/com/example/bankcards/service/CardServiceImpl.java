package com.example.bankcards.service;

import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.request.UpdateCardRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.BalanceResponse;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.exception.CardNotActiveException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.DuplicateCardNumberException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.UnauthorizedCardAccessException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.util.CardNumberUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final TransferRepository transferRepository;
    private final CardMapper cardMapper;
    private final CardNumberUtils cardNumberUtils;

    public CardServiceImpl(CardRepository cardRepository,
                           TransferRepository transferRepository,
                           CardMapper cardMapper,
                           CardNumberUtils cardNumberUtils) {
        this.cardRepository = cardRepository;
        this.transferRepository = transferRepository;
        this.cardMapper = cardMapper;
        this.cardNumberUtils = cardNumberUtils;
    }

    @Override
    @Transactional
    public CardResponse createCard(CreateCardRequest request) {
        if (!cardNumberUtils.isValidLuhn(request.getNumber())) {
            throw new IllegalArgumentException("Invalid card number: Luhn check failed");
        }
        var numberHash = cardNumberUtils.hash(request.getNumber());
        if (cardRepository.existsByNumberHash(numberHash)) {
            throw new DuplicateCardNumberException();
        }
        var card = cardMapper.toEntity(request);
        card.setNumberHash(numberHash);
        card.setLastFour(cardNumberUtils.lastFour(request.getNumber()));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);
        card = cardRepository.save(card);
        return toResponse(card);
    }

    @Override
    public CardResponse getCard(UUID id, UUID userId) {
        var card = cardRepository.findById(id)
            .orElseThrow(() -> new CardNotFoundException(id));
        if (!card.getUser().getId().equals(userId)) {
            throw new UnauthorizedCardAccessException(id);
        }
        return toResponse(card);
    }

    @Override
    public CardResponse getCardAdmin(UUID id) {
        var card = cardRepository.findById(id)
            .orElseThrow(() -> new CardNotFoundException(id));
        return toResponse(card);
    }

    @Override
    public PageResponse<CardResponse> getUserCards(UUID userId, int page, int size,
                                                    String status, String holder, LocalDate expirationDate) {
        var pageable = PageRequest.of(page, size, Sort.by("holder"));

        Specification<Card> spec = (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), CardStatus.valueOf(status.toUpperCase())));
            }
            if (holder != null && !holder.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("holder")), "%" + holder.toLowerCase() + "%"));
            }
            if (expirationDate != null) {
                predicates.add(cb.equal(root.get("expirationDate"), expirationDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        var pageResult = cardRepository.findAll(spec, pageable);
        var cards = pageResult.getContent().stream()
            .map(this::toResponse)
            .toList();

        return new PageResponse<>(cards, page, size, pageResult.getTotalElements(), pageResult.getTotalPages());
    }

    @Override
    public PageResponse<CardResponse> getAllCards(int page, int size, String status,
                                                   String holder, LocalDate expirationDate) {
        var pageable = PageRequest.of(page, size, Sort.by("holder"));

        Specification<Card> spec = (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), CardStatus.valueOf(status.toUpperCase())));
            }
            if (holder != null && !holder.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("holder")), "%" + holder.toLowerCase() + "%"));
            }
            if (expirationDate != null) {
                predicates.add(cb.equal(root.get("expirationDate"), expirationDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        var pageResult = cardRepository.findAll(spec, pageable);
        var cards = pageResult.getContent().stream()
            .map(this::toResponse)
            .toList();

        return new PageResponse<>(cards, page, size, pageResult.getTotalElements(), pageResult.getTotalPages());
    }

    @Override
    @Transactional
    public CardResponse updateCard(UUID id, UpdateCardRequest request) {
        var card = cardRepository.findById(id)
            .orElseThrow(() -> new CardNotFoundException(id));
        if (request.getHolder() != null) {
            card.setHolder(request.getHolder());
        }
        if (request.getExpirationDate() != null) {
            card.setExpirationDate(request.getExpirationDate());
        }
        card = cardRepository.save(card);
        return toResponse(card);
    }

    @Override
    @Transactional
    public void deleteCard(UUID id) {
        if (!cardRepository.existsById(id)) {
            throw new CardNotFoundException(id);
        }
        cardRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void blockCard(UUID id) {
        var card = cardRepository.findById(id)
            .orElseThrow(() -> new CardNotFoundException(id));
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    @Override
    @Transactional
    public void activateCard(UUID id) {
        var card = cardRepository.findById(id)
            .orElseThrow(() -> new CardNotFoundException(id));
        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
    }

    @Override
    @Transactional
    public void blockOwnCard(UUID id, UUID userId) {
        var card = cardRepository.findById(id)
            .orElseThrow(() -> new CardNotFoundException(id));
        if (!card.getUser().getId().equals(userId)) {
            throw new UnauthorizedCardAccessException(id);
        }
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    @Override
    @Transactional
    public void transfer(TransferRequest request, UUID userId) {
        if (request.getFromCardId().equals(request.getToCardId())) {
            throw new IllegalArgumentException("Cannot transfer to the same card");
        }

        var fromCard = cardRepository.findById(request.getFromCardId())
            .orElseThrow(() -> new CardNotFoundException(request.getFromCardId()));
        var toCard = cardRepository.findById(request.getToCardId())
            .orElseThrow(() -> new CardNotFoundException(request.getToCardId()));

        if (!fromCard.getUser().getId().equals(userId) || !toCard.getUser().getId().equals(userId)) {
            throw new UnauthorizedCardAccessException(request.getFromCardId());
        }

        checkExpired(fromCard);
        checkExpired(toCard);

        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardNotActiveException(request.getFromCardId());
        }
        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardNotActiveException(request.getToCardId());
        }

        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(fromCard.getBalance(), request.getAmount());
        }

        fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
        toCard.setBalance(toCard.getBalance().add(request.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        var transfer = new Transfer();
        transfer.setFromCard(fromCard);
        transfer.setToCard(toCard);
        transfer.setAmount(request.getAmount());
        transfer.setCreatedAt(LocalDateTime.now());
        transferRepository.save(transfer);
    }

    @Override
    public BalanceResponse getBalance(UUID id, UUID userId) {
        var card = cardRepository.findById(id)
            .orElseThrow(() -> new CardNotFoundException(id));
        if (!card.getUser().getId().equals(userId)) {
            throw new UnauthorizedCardAccessException(id);
        }
        return new BalanceResponse(card.getId(),
            cardNumberUtils.mask(card.getLastFour()), card.getBalance());
    }

    private void checkExpired(Card card) {
        if (card.getExpirationDate().isBefore(LocalDate.now())) {
            card.setStatus(CardStatus.EXPIRED);
        }
    }

    private CardResponse toResponse(Card card) {
        var masked = cardNumberUtils.mask(card.getLastFour());
        return new CardResponse(card.getId(), masked, card.getHolder(),
            card.getExpirationDate(), card.getStatus(), card.getBalance());
    }
}
