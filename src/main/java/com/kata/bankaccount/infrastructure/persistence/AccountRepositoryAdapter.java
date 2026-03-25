package com.kata.bankaccount.infrastructure.persistence;

import com.kata.bankaccount.domain.model.*;
import com.kata.bankaccount.domain.port.out.AccountRepository;
import com.kata.bankaccount.domain.port.out.TransactionRepository;
import com.kata.bankaccount.infrastructure.persistence.entity.AccountEntity;
import com.kata.bankaccount.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Component
public class AccountRepositoryAdapter implements AccountRepository {

    private final JpaAccountRepository jpaAccountRepository;
    private final JpaTransactionRepository jpaTransactionRepository;

    public AccountRepositoryAdapter(JpaAccountRepository jpaAccountRepository,
                                    JpaTransactionRepository jpaTransactionRepository) {
        this.jpaAccountRepository = jpaAccountRepository;
        this.jpaTransactionRepository = jpaTransactionRepository;
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity = new AccountEntity(
                account.id().value(),
                account.ownerName(),
                account.balance().amount(),
                account.balance().currency().getCurrencyCode(),
                account.createdAt()
        );
        jpaAccountRepository.save(entity);
        return account;
    }

    @Override
    public Optional<Account> findById(AccountId accountId) {
        return jpaAccountRepository.findById(accountId.value())
                .map(entity -> {
                    List<Transaction> transactions = jpaTransactionRepository
                            .findByAccountIdOrderByOccurredAtAsc(entity.getId())
                            .stream()
                            .map(this::toDomain)
                            .toList();
                    return Account.reconstitute(
                            AccountId.of(entity.getId()),
                            entity.getOwnerName(),
                            Money.of(entity.getBalance(), Currency.getInstance(entity.getCurrency())),
                            transactions,
                            entity.getCreatedAt()
                    );
                });
    }

    @Override
    public List<Account> findAll() {
        return jpaAccountRepository.findAll().stream()
                .map(entity -> {
                    List<Transaction> transactions = jpaTransactionRepository
                            .findByAccountIdOrderByOccurredAtAsc(entity.getId())
                            .stream()
                            .map(this::toDomain)
                            .toList();
                    return Account.reconstitute(
                            AccountId.of(entity.getId()),
                            entity.getOwnerName(),
                            Money.of(entity.getBalance(), Currency.getInstance(entity.getCurrency())),
                            transactions,
                            entity.getCreatedAt()
                    );
                })
                .toList();
    }

    private Transaction toDomain(TransactionEntity entity) {
        Currency currency = Currency.getInstance(entity.getCurrency());
        return new Transaction(
                TransactionId.of(entity.getId()),
                entity.getType(),
                Money.of(entity.getAmount(), currency),
                Money.of(entity.getBalanceAfter(), currency),
                entity.getOccurredAt()
        );
    }
}
