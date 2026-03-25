package fr.kanis.bankaccount.application.service;

import fr.kanis.bankaccount.domain.model.Account;
import fr.kanis.bankaccount.domain.model.AccountId;
import fr.kanis.bankaccount.domain.model.AccountNotFoundException;
import fr.kanis.bankaccount.domain.port.in.GetAccountUseCase;
import fr.kanis.bankaccount.domain.port.out.AccountRepository;

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
