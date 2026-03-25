package fr.kanis.bankaccount.domain.port.out;

import fr.kanis.bankaccount.domain.model.Account;
import fr.kanis.bankaccount.domain.model.AccountId;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(AccountId accountId);
    List<Account> findAll();
}
