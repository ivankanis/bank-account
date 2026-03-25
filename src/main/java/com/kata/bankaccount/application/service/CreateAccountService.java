package com.kata.bankaccount.application.service;

import com.kata.bankaccount.application.command.CreateAccountCommand;
import com.kata.bankaccount.domain.model.Account;
import com.kata.bankaccount.domain.model.Money;
import com.kata.bankaccount.domain.port.in.CreateAccountUseCase;
import com.kata.bankaccount.domain.port.out.AccountRepository;

import java.time.Instant;
import java.util.Currency;

public class CreateAccountService implements CreateAccountUseCase {

    private final AccountRepository accountRepository;

    public CreateAccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account createAccount(CreateAccountCommand command) {
        Money initialBalance = Money.of(command.initialAmount(), Currency.getInstance(command.currencyCode()));
        Account account = Account.open(command.ownerName(), initialBalance, Instant.now());
        return accountRepository.save(account);
    }
}
