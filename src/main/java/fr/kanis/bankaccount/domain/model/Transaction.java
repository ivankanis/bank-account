package fr.kanis.bankaccount.domain.model;

import java.time.Instant;
import java.util.Objects;

public record Transaction(
        TransactionId id,
        TransactionType type,
        Money amount,
        Money balanceAfter,
        Instant occurredAt
) {

    public Transaction {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(balanceAfter, "balanceAfter must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    public static Transaction deposit(Money amount, Money balanceAfter, Instant occurredAt) {
        return new Transaction(TransactionId.generate(), TransactionType.DEPOSIT, amount, balanceAfter, occurredAt);
    }

    public static Transaction withdrawal(Money amount, Money balanceAfter, Instant occurredAt) {
        return new Transaction(TransactionId.generate(), TransactionType.WITHDRAWAL, amount, balanceAfter, occurredAt);
    }
}
