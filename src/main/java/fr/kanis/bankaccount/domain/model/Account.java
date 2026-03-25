package fr.kanis.bankaccount.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Account {

    private final AccountId id;
    private final String ownerName;
    private Money balance;
    private final List<Transaction> transactions;
    private final Instant createdAt;

    private Account(AccountId id, String ownerName, Money balance, List<Transaction> transactions, Instant createdAt) {
        this.id = id;
        this.ownerName = ownerName;
        this.balance = balance;
        this.transactions = new ArrayList<>(transactions);
        this.createdAt = createdAt;
    }

    public static Account open(String ownerName, Money initialBalance, Instant now) {
        Objects.requireNonNull(initialBalance, "initialBalance must not be null");
        Objects.requireNonNull(now, "now must not be null");
        if (ownerName == null || ownerName.isBlank()) {
            throw new IllegalArgumentException("Owner name must not be blank");
        }
        return new Account(AccountId.generate(), ownerName, initialBalance, List.of(), now);
    }

    public static Account reconstitute(AccountId id, String ownerName, Money balance,
                                       List<Transaction> transactions, Instant createdAt) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(ownerName);
        Objects.requireNonNull(balance);
        Objects.requireNonNull(transactions);
        Objects.requireNonNull(createdAt);
        return new Account(id, ownerName, balance, transactions, createdAt);
    }

    public Transaction deposit(Money amount, Instant now) {
        Objects.requireNonNull(amount, "amount must not be null");
        if (amount.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        this.balance = this.balance.add(amount);
        Transaction transaction = Transaction.deposit(amount, this.balance, now);
        this.transactions.add(transaction);
        return transaction;
    }

    public Transaction withdraw(Money amount, Instant now) {
        Objects.requireNonNull(amount, "amount must not be null");
        if (amount.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (!this.balance.isGreaterThanOrEqual(amount)) {
            throw new InsufficientFundsException(this.id, this.balance, amount);
        }
        this.balance = this.balance.subtract(amount);
        Transaction transaction = Transaction.withdrawal(amount, this.balance, now);
        this.transactions.add(transaction);
        return transaction;
    }

    public AccountId id() {
        return id;
    }

    public String ownerName() {
        return ownerName;
    }

    public Money balance() {
        return balance;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public List<Transaction> transactions() {
        return List.copyOf(transactions);
    }
}
