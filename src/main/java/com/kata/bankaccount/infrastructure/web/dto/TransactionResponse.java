package com.kata.bankaccount.infrastructure.web.dto;

import com.kata.bankaccount.domain.model.Transaction;
import com.kata.bankaccount.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID transactionId,
        TransactionType type,
        BigDecimal amount,
        String currencyCode,
        BigDecimal balanceAfter,
        Instant occurredAt
) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.id().value(),
                transaction.type(),
                transaction.amount().amount(),
                transaction.amount().currency().getCurrencyCode(),
                transaction.balanceAfter().amount(),
                transaction.occurredAt()
        );
    }
}
