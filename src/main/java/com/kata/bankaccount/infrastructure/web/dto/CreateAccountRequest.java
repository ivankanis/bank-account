package com.kata.bankaccount.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank(message = "ownerName must not be blank")
        String ownerName,

        @NotNull(message = "initialAmount must not be null")
        @DecimalMin(value = "0.00", message = "initialAmount must not be negative")
        BigDecimal initialAmount,

        @NotBlank(message = "currencyCode must not be blank")
        String currencyCode
) {}
