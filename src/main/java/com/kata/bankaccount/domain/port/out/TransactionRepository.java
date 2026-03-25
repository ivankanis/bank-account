package com.kata.bankaccount.domain.port.out;

import com.kata.bankaccount.domain.model.AccountId;
import com.kata.bankaccount.domain.model.Transaction;

import java.util.List;

public interface TransactionRepository {
    Transaction save(Transaction transaction, AccountId accountId);
    List<Transaction> findByAccountId(AccountId accountId);
}
