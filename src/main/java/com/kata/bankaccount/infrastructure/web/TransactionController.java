package com.kata.bankaccount.infrastructure.web;

import com.kata.bankaccount.application.command.DepositMoneyCommand;
import com.kata.bankaccount.application.command.WithdrawMoneyCommand;
import com.kata.bankaccount.domain.model.AccountId;
import com.kata.bankaccount.domain.port.in.DepositMoneyUseCase;
import com.kata.bankaccount.domain.port.in.GetTransactionHistoryUseCase;
import com.kata.bankaccount.domain.port.in.WithdrawMoneyUseCase;
import com.kata.bankaccount.infrastructure.web.dto.MoneyOperationRequest;
import com.kata.bankaccount.infrastructure.web.dto.TransactionHistoryResponse;
import com.kata.bankaccount.infrastructure.web.dto.TransactionResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts/{accountId}")
@CrossOrigin
public class TransactionController {

    private final DepositMoneyUseCase depositMoneyUseCase;
    private final WithdrawMoneyUseCase withdrawMoneyUseCase;
    private final GetTransactionHistoryUseCase getTransactionHistoryUseCase;

    public TransactionController(DepositMoneyUseCase depositMoneyUseCase,
                                 WithdrawMoneyUseCase withdrawMoneyUseCase,
                                 GetTransactionHistoryUseCase getTransactionHistoryUseCase) {
        this.depositMoneyUseCase = depositMoneyUseCase;
        this.withdrawMoneyUseCase = withdrawMoneyUseCase;
        this.getTransactionHistoryUseCase = getTransactionHistoryUseCase;
    }

    @PostMapping("/deposits")
    public TransactionResponse deposit(@PathVariable UUID accountId,
                                       @Valid @RequestBody MoneyOperationRequest request) {
        var command = new DepositMoneyCommand(AccountId.of(accountId), request.amount(), request.currencyCode());
        return TransactionResponse.from(depositMoneyUseCase.deposit(command));
    }

    @PostMapping("/withdrawals")
    public TransactionResponse withdraw(@PathVariable UUID accountId,
                                        @Valid @RequestBody MoneyOperationRequest request) {
        var command = new WithdrawMoneyCommand(AccountId.of(accountId), request.amount(), request.currencyCode());
        return TransactionResponse.from(withdrawMoneyUseCase.withdraw(command));
    }

    @GetMapping("/transactions")
    public TransactionHistoryResponse getHistory(@PathVariable UUID accountId) {
        var history = getTransactionHistoryUseCase.getHistory(AccountId.of(accountId));
        return TransactionHistoryResponse.from(accountId, history);
    }
}
