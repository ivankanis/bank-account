package com.kata.bankaccount.domain.port.in;

import com.kata.bankaccount.application.command.WithdrawMoneyCommand;
import com.kata.bankaccount.domain.model.Transaction;

public interface WithdrawMoneyUseCase {
    Transaction withdraw(WithdrawMoneyCommand command);
}
