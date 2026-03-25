package fr.kanis.bankaccount.application.service;

import fr.kanis.bankaccount.application.command.WithdrawMoneyCommand;
import fr.kanis.bankaccount.domain.model.Account;
import fr.kanis.bankaccount.domain.model.AccountNotFoundException;
import fr.kanis.bankaccount.domain.model.Money;
import fr.kanis.bankaccount.domain.model.Transaction;
import fr.kanis.bankaccount.domain.port.in.WithdrawMoneyUseCase;
import fr.kanis.bankaccount.domain.port.out.AccountRepository;
import fr.kanis.bankaccount.domain.port.out.TransactionRepository;

import java.time.Instant;
import java.util.Currency;

public class WithdrawMoneyService implements WithdrawMoneyUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public WithdrawMoneyService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction withdraw(WithdrawMoneyCommand command) {
        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        Money amount = Money.of(command.amount(), Currency.getInstance(command.currencyCode()));
        Transaction transaction = account.withdraw(amount, Instant.now());

        accountRepository.save(account);
        return transactionRepository.save(transaction, account.id());
    }
}
