package com.kata.bankaccount.application.service;

import com.kata.bankaccount.domain.model.AccountId;
import com.kata.bankaccount.domain.model.AccountNotFoundException;
import com.kata.bankaccount.domain.model.Transaction;
import com.kata.bankaccount.domain.port.in.GetTransactionHistoryUseCase;
import com.kata.bankaccount.domain.port.out.AccountRepository;
import com.kata.bankaccount.domain.port.out.TransactionRepository;

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
