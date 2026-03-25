package fr.kanis.bankaccount.infrastructure.web.dto;

import fr.kanis.bankaccount.domain.model.Account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID accountId,
        String ownerName,
        BigDecimal balance,
        String currencyCode,
        Instant createdAt
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.id().value(),
                account.ownerName(),
                account.balance().amount(),
                account.balance().currency().getCurrencyCode(),
                account.createdAt()
        );
    }
}
