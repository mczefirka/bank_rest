package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID>, JpaSpecificationExecutor<Card> {
    Page<Card> findByUserId(UUID userId, Pageable pageable);

    Optional<Card> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByNumberHash(String numberHash);
}
