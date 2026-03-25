package com.kata.bankaccount.application.service;

import com.kata.bankaccount.domain.model.Account;
import com.kata.bankaccount.domain.model.AccountId;
import com.kata.bankaccount.domain.model.AccountNotFoundException;
import com.kata.bankaccount.domain.port.in.GetAccountUseCase;
import com.kata.bankaccount.domain.port.out.AccountRepository;

import java.util.List;

public class GetAccountService implements GetAccountUseCase {

    private final AccountRepository accountRepository;

    public GetAccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account getAccount(AccountId accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
}
