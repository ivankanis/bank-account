package com.kata.bankaccount.e2e;

import com.kata.bankaccount.infrastructure.web.dto.CreateAccountRequest;
import com.kata.bankaccount.infrastructure.web.dto.MoneyOperationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BankAccountE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String BASE = "/api/v1/accounts";

    @Test
    void full_lifecycle_deposit_withdraw_history() {
        // Create account
        var createReq = new CreateAccountRequest("Alice", new BigDecimal("100.00"), "EUR");
        ResponseEntity<Map> createResp = restTemplate.postForEntity(BASE, createReq, Map.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID accountId = UUID.fromString((String) createResp.getBody().get("accountId"));

        // Deposit
        var depositReq = new MoneyOperationRequest(new BigDecimal("50.00"), "EUR");
        ResponseEntity<Map> depositResp = restTemplate.postForEntity(
                BASE + "/" + accountId + "/deposits", depositReq, Map.class);
        assertThat(depositResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(depositResp.getBody().get("type")).isEqualTo("DEPOSIT");
        assertThat(depositResp.getBody().get("balanceAfter")).isEqualTo(150.0);

        // Withdraw
        var withdrawReq = new MoneyOperationRequest(new BigDecimal("30.00"), "EUR");
        ResponseEntity<Map> withdrawResp = restTemplate.postForEntity(
                BASE + "/" + accountId + "/withdrawals", withdrawReq, Map.class);
        assertThat(withdrawResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(withdrawResp.getBody().get("type")).isEqualTo("WITHDRAWAL");
        assertThat(withdrawResp.getBody().get("balanceAfter")).isEqualTo(120.0);

        // History
        ResponseEntity<Map> historyResp = restTemplate.getForEntity(
                BASE + "/" + accountId + "/transactions", Map.class);
        assertThat(historyResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        var transactions = (java.util.List<?>) historyResp.getBody().get("transactions");
        assertThat(transactions).hasSize(2);
    }

    @Test
    void withdraw_more_than_balance_returns_422() {
        var createReq = new CreateAccountRequest("Bob", new BigDecimal("50.00"), "EUR");
        ResponseEntity<Map> createResp = restTemplate.postForEntity(BASE, createReq, Map.class);
        UUID accountId = UUID.fromString((String) createResp.getBody().get("accountId"));

        var withdrawReq = new MoneyOperationRequest(new BigDecimal("200.00"), "EUR");
        ResponseEntity<Map> resp = restTemplate.postForEntity(
                BASE + "/" + accountId + "/withdrawals", withdrawReq, Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(resp.getBody().get("errorCode")).isEqualTo("INSUFFICIENT_FUNDS");
    }

    @Test
    void deposit_to_unknown_account_returns_404() {
        var req = new MoneyOperationRequest(new BigDecimal("50.00"), "EUR");
        ResponseEntity<Map> resp = restTemplate.postForEntity(
                BASE + "/" + UUID.randomUUID() + "/deposits", req, Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody().get("errorCode")).isEqualTo("ACCOUNT_NOT_FOUND");
    }

    @Test
    void create_account_with_blank_name_returns_400() {
        var req = new CreateAccountRequest("", new BigDecimal("100.00"), "EUR");
        ResponseEntity<Map> resp = restTemplate.postForEntity(BASE, req, Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().get("errorCode")).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void withdraw_full_balance_succeeds_leaving_zero() {
        var createReq = new CreateAccountRequest("Carol", new BigDecimal("100.00"), "EUR");
        ResponseEntity<Map> createResp = restTemplate.postForEntity(BASE, createReq, Map.class);
        UUID accountId = UUID.fromString((String) createResp.getBody().get("accountId"));

        var withdrawReq = new MoneyOperationRequest(new BigDecimal("100.00"), "EUR");
        ResponseEntity<Map> resp = restTemplate.postForEntity(
                BASE + "/" + accountId + "/withdrawals", withdrawReq, Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("balanceAfter")).isEqualTo(0.0);
    }

    @Test
    void multiple_accounts_are_independent() {
        var req1 = new CreateAccountRequest("Alice", new BigDecimal("100.00"), "EUR");
        var req2 = new CreateAccountRequest("Bob", new BigDecimal("200.00"), "EUR");
        UUID id1 = UUID.fromString((String) restTemplate.postForEntity(BASE, req1, Map.class).getBody().get("accountId"));
        UUID id2 = UUID.fromString((String) restTemplate.postForEntity(BASE, req2, Map.class).getBody().get("accountId"));

        restTemplate.postForEntity(BASE + "/" + id1 + "/deposits",
                new MoneyOperationRequest(new BigDecimal("50.00"), "EUR"), Map.class);

        ResponseEntity<Map> alice = restTemplate.getForEntity(BASE + "/" + id1, Map.class);
        ResponseEntity<Map> bob = restTemplate.getForEntity(BASE + "/" + id2, Map.class);

        assertThat(alice.getBody().get("balance")).isEqualTo(150.0);
        assertThat(bob.getBody().get("balance")).isEqualTo(200.0);
    }
}
