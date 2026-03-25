package com.kata.bankaccount.domain.port.in;

import com.kata.bankaccount.domain.model.AccountId;
import com.kata.bankaccount.domain.model.Transaction;

import java.util.List;

public interface GetTransactionHistoryUseCase {
    List<Transaction> getHistory(AccountId accountId);
}
