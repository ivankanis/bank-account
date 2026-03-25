package com.kata.bankaccount.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kata.bankaccount.domain.model.Account;
import com.kata.bankaccount.domain.model.AccountId;
import com.kata.bankaccount.domain.model.AccountNotFoundException;
import com.kata.bankaccount.domain.model.Money;
import com.kata.bankaccount.domain.port.in.CreateAccountUseCase;
import com.kata.bankaccount.domain.port.in.GetAccountUseCase;
import com.kata.bankaccount.infrastructure.config.BeanConfig;
import com.kata.bankaccount.infrastructure.web.dto.CreateAccountRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AccountController.class, GlobalExceptionHandler.class})
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateAccountUseCase createAccountUseCase;

    @MockBean
    private GetAccountUseCase getAccountUseCase;

    @Test
    void create_account_returns_201_with_account_body() throws Exception {
        Account account = Account.open("Alice", Money.of("100.00", "EUR"), Instant.now());
        when(createAccountUseCase.createAccount(any())).thenReturn(account);

        var request = new CreateAccountRequest("Alice", new BigDecimal("100.00"), "EUR");

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerName").value("Alice"))
                .andExpect(jsonPath("$.balance").value(100.00))
                .andExpect(jsonPath("$.currencyCode").value("EUR"));
    }

    @Test
    void create_account_with_blank_owner_returns_400() throws Exception {
        var request = new CreateAccountRequest("", new BigDecimal("100.00"), "EUR");

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void create_account_with_null_amount_returns_400() throws Exception {
        var body = "{\"ownerName\":\"Alice\",\"currencyCode\":\"EUR\"}";

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void get_all_accounts_returns_list() throws Exception {
        Account account = Account.open("Alice", Money.of("100.00", "EUR"), Instant.now());
        when(getAccountUseCase.getAllAccounts()).thenReturn(List.of(account));

        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ownerName").value("Alice"));
    }

    @Test
    void get_unknown_account_returns_404() throws Exception {
        AccountId id = AccountId.generate();
        when(getAccountUseCase.getAccount(any())).thenThrow(new AccountNotFoundException(id));

        mockMvc.perform(get("/api/v1/accounts/" + id.value()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ACCOUNT_NOT_FOUND"));
    }
}
