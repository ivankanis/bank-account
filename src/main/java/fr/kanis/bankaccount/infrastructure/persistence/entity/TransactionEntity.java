package fr.kanis.bankaccount.infrastructure.persistence.entity;

import fr.kanis.bankaccount.domain.model.TransactionType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TransactionType type;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected TransactionEntity() {}

    public TransactionEntity(UUID id, UUID accountId, TransactionType type,
                             BigDecimal amount, String currency,
                             BigDecimal balanceAfter, Instant occurredAt) {
        this.id = id;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.balanceAfter = balanceAfter;
        this.occurredAt = occurredAt;
    }

    public UUID getId() { return id; }
    public UUID getAccountId() { return accountId; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public Instant getOccurredAt() { return occurredAt; }
}
