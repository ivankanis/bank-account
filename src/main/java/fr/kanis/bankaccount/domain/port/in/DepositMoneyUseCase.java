package fr.kanis.bankaccount.domain.port.in;

import fr.kanis.bankaccount.application.command.DepositMoneyCommand;
import fr.kanis.bankaccount.domain.model.Transaction;

public interface DepositMoneyUseCase {
    Transaction deposit(DepositMoneyCommand command);
}
