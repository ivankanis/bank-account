package fr.kanis.bankaccount.application.service;

import fr.kanis.bankaccount.application.command.CreateAccountCommand;
import fr.kanis.bankaccount.domain.model.Account;
import fr.kanis.bankaccount.domain.model.Money;
import fr.kanis.bankaccount.domain.port.in.CreateAccountUseCase;
import fr.kanis.bankaccount.domain.port.out.AccountRepository;

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
