package fr.kanis.bankaccount.infrastructure.web.dto;

import fr.kanis.bankaccount.domain.model.Transaction;
import fr.kanis.bankaccount.domain.model.TransactionType;

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
