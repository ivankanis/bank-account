package fr.kanis.bankaccount.infrastructure.persistence;

import fr.kanis.bankaccount.domain.model.AccountId;
import fr.kanis.bankaccount.domain.model.Transaction;
import fr.kanis.bankaccount.domain.port.out.TransactionRepository;
import fr.kanis.bankaccount.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.List;

@Component
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final JpaTransactionRepository jpaTransactionRepository;

    public TransactionRepositoryAdapter(JpaTransactionRepository jpaTransactionRepository) {
        this.jpaTransactionRepository = jpaTransactionRepository;
    }

    @Override
    public Transaction save(Transaction transaction, AccountId accountId) {
        TransactionEntity entity = new TransactionEntity(
                transaction.id().value(),
                accountId.value(),
                transaction.type(),
                transaction.amount().amount(),
                transaction.amount().currency().getCurrencyCode(),
                transaction.balanceAfter().amount(),
                transaction.occurredAt()
        );
        jpaTransactionRepository.save(entity);
        return transaction;
    }

    @Override
    public List<Transaction> findByAccountId(AccountId accountId) {
        return jpaTransactionRepository
                .findByAccountIdOrderByOccurredAtAsc(accountId.value())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Transaction toDomain(TransactionEntity entity) {
        Currency currency = Currency.getInstance(entity.getCurrency());
        return new Transaction(
                fr.kanis.bankaccount.domain.model.TransactionId.of(entity.getId()),
                entity.getType(),
                fr.kanis.bankaccount.domain.model.Money.of(entity.getAmount(), currency),
                fr.kanis.bankaccount.domain.model.Money.of(entity.getBalanceAfter(), currency),
                entity.getOccurredAt()
        );
    }
}
