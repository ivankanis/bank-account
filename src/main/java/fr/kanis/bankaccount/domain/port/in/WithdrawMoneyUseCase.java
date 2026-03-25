package fr.kanis.bankaccount.domain.port.in;

import fr.kanis.bankaccount.application.command.WithdrawMoneyCommand;
import fr.kanis.bankaccount.domain.model.Transaction;

public interface WithdrawMoneyUseCase {
    Transaction withdraw(WithdrawMoneyCommand command);
}
