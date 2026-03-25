package fr.kanis.bankaccount.domain.port.out;

import fr.kanis.bankaccount.domain.model.AccountId;
import fr.kanis.bankaccount.domain.model.Transaction;

import java.util.List;

public interface TransactionRepository {
    Transaction save(Transaction transaction, AccountId accountId);
    List<Transaction> findByAccountId(AccountId accountId);
}
