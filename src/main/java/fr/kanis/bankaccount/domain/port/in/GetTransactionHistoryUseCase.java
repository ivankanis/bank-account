package fr.kanis.bankaccount.domain.port.in;

import fr.kanis.bankaccount.domain.model.AccountId;
import fr.kanis.bankaccount.domain.model.Transaction;

import java.util.List;

public interface GetTransactionHistoryUseCase {
    List<Transaction> getHistory(AccountId accountId);
}
