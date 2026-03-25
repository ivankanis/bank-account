package fr.kanis.bankaccount.infrastructure.web.dto;

import fr.kanis.bankaccount.domain.model.Transaction;

import java.util.List;
import java.util.UUID;

public record TransactionHistoryResponse(UUID accountId, List<TransactionResponse> transactions) {

    public static TransactionHistoryResponse from(UUID accountId, List<Transaction> transactions) {
        return new TransactionHistoryResponse(
                accountId,
                transactions.stream().map(TransactionResponse::from).toList()
        );
    }
}
