package com.kata.bankaccount.domain.port.in;

import com.kata.bankaccount.application.command.DepositMoneyCommand;
import com.kata.bankaccount.domain.model.Transaction;

public interface DepositMoneyUseCase {
    Transaction deposit(DepositMoneyCommand command);
}
