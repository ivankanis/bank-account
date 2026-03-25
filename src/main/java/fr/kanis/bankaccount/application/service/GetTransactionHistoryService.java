package fr.kanis.bankaccount.application.service;

import fr.kanis.bankaccount.domain.model.AccountId;
import fr.kanis.bankaccount.domain.model.AccountNotFoundException;
import fr.kanis.bankaccount.domain.model.Transaction;
import fr.kanis.bankaccount.domain.port.in.GetTransactionHistoryUseCase;
import fr.kanis.bankaccount.domain.port.out.AccountRepository;
import fr.kanis.bankaccount.domain.port.out.TransactionRepository;

import java.util.List;

public class GetTransactionHistoryService implements GetTransactionHistoryUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public GetTransactionHistoryService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public List<Transaction> getHistory(AccountId accountId) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        return transactionRepository.findByAccountId(accountId);
    }
}
