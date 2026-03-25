package com.kata.bankaccount.infrastructure.persistence;

import com.kata.bankaccount.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface JpaTransactionRepository extends JpaRepository<TransactionEntity, UUID> {
    List<TransactionEntity> findByAccountIdOrderByOccurredAtAsc(UUID accountId);
}
