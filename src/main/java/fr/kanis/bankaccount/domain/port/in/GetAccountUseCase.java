package fr.kanis.bankaccount.domain.port.in;

import fr.kanis.bankaccount.domain.model.Account;
import fr.kanis.bankaccount.domain.model.AccountId;

import java.util.List;

public interface GetAccountUseCase {
    Account getAccount(AccountId accountId);
    List<Account> getAllAccounts();
}
