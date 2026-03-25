package com.kata.bankaccount.domain.port.in;

import com.kata.bankaccount.domain.model.Account;
import com.kata.bankaccount.domain.model.AccountId;

import java.util.List;

public interface GetAccountUseCase {
    Account getAccount(AccountId accountId);
    List<Account> getAllAccounts();
}
