package com.kata.bankaccount.application.command;

import java.math.BigDecimal;
import java.util.Objects;

public record CreateAccountCommand(String ownerName, BigDecimal initialAmount, String currencyCode) {

    public CreateAccountCommand {
        if (ownerName == null || ownerName.isBlank()) {
            throw new IllegalArgumentException("ownerName must not be blank");
        }
        Objects.requireNonNull(initialAmount, "initialAmount must not be null");
        if (initialAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("initialAmount must not be negative");
        }
        Objects.requireNonNull(currencyCode, "currencyCode must not be null");
    }
}
