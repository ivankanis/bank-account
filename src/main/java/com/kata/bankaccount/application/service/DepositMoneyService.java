package com.kata.bankaccount.application.service;

import com.kata.bankaccount.application.command.DepositMoneyCommand;
import com.kata.bankaccount.domain.model.Account;
import com.kata.bankaccount.domain.model.AccountNotFoundException;
import com.kata.bankaccount.domain.model.Money;
import com.kata.bankaccount.domain.model.Transaction;
import com.kata.bankaccount.domain.port.in.DepositMoneyUseCase;
import com.kata.bankaccount.domain.port.out.AccountRepository;
import com.kata.bankaccount.domain.port.out.TransactionRepository;

import java.time.Instant;
import java.util.Currency;

public class DepositMoneyService implements DepositMoneyUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public DepositMoneyService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction deposit(DepositMoneyCommand command) {
        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        Money amount = Money.of(command.amount(), Currency.getInstance(command.currencyCode()));
        Transaction transaction = account.deposit(amount, Instant.now());

        accountRepository.save(account);
        return transactionRepository.save(transaction, account.id());
    }
}
