package com.kata.bankaccount.domain.port.out;

import com.kata.bankaccount.domain.model.Account;
import com.kata.bankaccount.domain.model.AccountId;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(AccountId accountId);
    List<Account> findAll();
}
