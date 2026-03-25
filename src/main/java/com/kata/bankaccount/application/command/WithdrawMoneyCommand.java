package com.kata.bankaccount.application.command;

import com.kata.bankaccount.domain.model.AccountId;

import java.math.BigDecimal;
import java.util.Objects;

public record WithdrawMoneyCommand(AccountId accountId, BigDecimal amount, String currencyCode) {

    public WithdrawMoneyCommand {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(currencyCode, "currencyCode must not be null");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
    }
}
