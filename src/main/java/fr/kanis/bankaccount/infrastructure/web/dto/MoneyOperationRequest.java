package fr.kanis.bankaccount.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MoneyOperationRequest(
        @NotNull(message = "amount must not be null")
        @DecimalMin(value = "0.01", message = "amount must be positive")
        BigDecimal amount,

        @NotBlank(message = "currencyCode must not be blank")
        String currencyCode
) {}
