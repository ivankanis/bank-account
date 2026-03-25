package com.kata.bankaccount.domain.port.in;

import com.kata.bankaccount.application.command.CreateAccountCommand;
import com.kata.bankaccount.domain.model.Account;

public interface CreateAccountUseCase {
    Account createAccount(CreateAccountCommand command);
}
