package fr.kanis.bankaccount.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.kanis.bankaccount.domain.model.*;
import fr.kanis.bankaccount.domain.port.in.DepositMoneyUseCase;
import fr.kanis.bankaccount.domain.port.in.GetTransactionHistoryUseCase;
import fr.kanis.bankaccount.domain.port.in.WithdrawMoneyUseCase;
import fr.kanis.bankaccount.infrastructure.web.dto.MoneyOperationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {TransactionController.class, GlobalExceptionHandler.class})
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DepositMoneyUseCase depositMoneyUseCase;

    @MockBean
    private WithdrawMoneyUseCase withdrawMoneyUseCase;

    @MockBean
    private GetTransactionHistoryUseCase getTransactionHistoryUseCase;

    @Test
    void deposit_returns_transaction_response() throws Exception {
        UUID accountId = UUID.randomUUID();
        Transaction tx = Transaction.deposit(
                Money.of("50.00", "EUR"),
                Money.of("150.00", "EUR"),
                Instant.now()
        );
        when(depositMoneyUseCase.deposit(any())).thenReturn(tx);

        var request = new MoneyOperationRequest(new BigDecimal("50.00"), "EUR");
        mockMvc.perform(post("/api/v1/accounts/" + accountId + "/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.balanceAfter").value(150.00));
    }

    @Test
    void deposit_to_unknown_account_returns_404() throws Exception {
        UUID accountId = UUID.randomUUID();
        when(depositMoneyUseCase.deposit(any()))
                .thenThrow(new AccountNotFoundException(AccountId.of(accountId)));

        var request = new MoneyOperationRequest(new BigDecimal("50.00"), "EUR");
        mockMvc.perform(post("/api/v1/accounts/" + accountId + "/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ACCOUNT_NOT_FOUND"));
    }

    @Test
    void withdraw_with_insufficient_funds_returns_422() throws Exception {
        UUID accountId = UUID.randomUUID();
        when(withdrawMoneyUseCase.withdraw(any()))
                .thenThrow(new InsufficientFundsException(
                        AccountId.of(accountId),
                        Money.of("10.00", "EUR"),
                        Money.of("100.00", "EUR")
                ));

        var request = new MoneyOperationRequest(new BigDecimal("100.00"), "EUR");
        mockMvc.perform(post("/api/v1/accounts/" + accountId + "/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_FUNDS"));
    }

    @Test
    void get_history_returns_ordered_transactions() throws Exception {
        UUID accountId = UUID.randomUUID();
        Instant t1 = Instant.parse("2026-01-01T10:00:00Z");
        Instant t2 = Instant.parse("2026-01-01T11:00:00Z");
        List<Transaction> history = List.of(
                Transaction.deposit(Money.of("100.00", "EUR"), Money.of("100.00", "EUR"), t1),
                Transaction.withdrawal(Money.of("30.00", "EUR"), Money.of("70.00", "EUR"), t2)
        );
        when(getTransactionHistoryUseCase.getHistory(any())).thenReturn(history);

        mockMvc.perform(get("/api/v1/accounts/" + accountId + "/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.transactions.length()").value(2))
                .andExpect(jsonPath("$.transactions[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$.transactions[1].type").value("WITHDRAWAL"));
    }

    @Test
    void deposit_with_zero_amount_returns_400() throws Exception {
        UUID accountId = UUID.randomUUID();
        var request = new MoneyOperationRequest(BigDecimal.ZERO, "EUR");

        mockMvc.perform(post("/api/v1/accounts/" + accountId + "/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }
}
