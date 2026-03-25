package fr.kanis.bankaccount.domain.port.in;

import fr.kanis.bankaccount.application.command.CreateAccountCommand;
import fr.kanis.bankaccount.domain.model.Account;

public interface CreateAccountUseCase {
    Account createAccount(CreateAccountCommand command);
}
