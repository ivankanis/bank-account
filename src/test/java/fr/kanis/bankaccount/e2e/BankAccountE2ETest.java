package fr.kanis.bankaccount.e2e;

import fr.kanis.bankaccount.infrastructure.web.dto.CreateAccountRequest;
import fr.kanis.bankaccount.infrastructure.web.dto.MoneyOperationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    private ResponseEntity<Map<String, Object>> post(String url, Object body) {
        return restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body), MAP_TYPE);
    }

    private ResponseEntity<Map<String, Object>> get(String url) {
        return restTemplate.exchange(url, HttpMethod.GET, null, MAP_TYPE);
    }

    @Test
    void full_lifecycle_deposit_withdraw_history() {
        // Create account
        var createReq = new CreateAccountRequest("Alice", new BigDecimal("100.00"), "EUR");
        ResponseEntity<Map<String, Object>> createResp = post(BASE, createReq);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID accountId = UUID.fromString((String) createResp.getBody().get("accountId"));

        // Deposit
        var depositReq = new MoneyOperationRequest(new BigDecimal("50.00"), "EUR");
        ResponseEntity<Map<String, Object>> depositResp = post(BASE + "/" + accountId + "/deposits", depositReq);
        assertThat(depositResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(depositResp.getBody().get("type")).isEqualTo("DEPOSIT");
        assertThat(depositResp.getBody().get("balanceAfter")).isEqualTo(150.0);

        // Withdraw
        var withdrawReq = new MoneyOperationRequest(new BigDecimal("30.00"), "EUR");
        ResponseEntity<Map<String, Object>> withdrawResp = post(BASE + "/" + accountId + "/withdrawals", withdrawReq);
        assertThat(withdrawResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(withdrawResp.getBody().get("type")).isEqualTo("WITHDRAWAL");
        assertThat(withdrawResp.getBody().get("balanceAfter")).isEqualTo(120.0);

        // History
        ResponseEntity<Map<String, Object>> historyResp = get(BASE + "/" + accountId + "/transactions");
        assertThat(historyResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        var transactions = (java.util.List<?>) historyResp.getBody().get("transactions");
        assertThat(transactions).hasSize(2);
    }

    @Test
    void withdraw_more_than_balance_returns_422() {
        var createReq = new CreateAccountRequest("Bob", new BigDecimal("50.00"), "EUR");
        ResponseEntity<Map<String, Object>> createResp = post(BASE, createReq);
        UUID accountId = UUID.fromString((String) createResp.getBody().get("accountId"));

        var withdrawReq = new MoneyOperationRequest(new BigDecimal("200.00"), "EUR");
        ResponseEntity<Map<String, Object>> resp = post(BASE + "/" + accountId + "/withdrawals", withdrawReq);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(resp.getBody().get("errorCode")).isEqualTo("INSUFFICIENT_FUNDS");
    }

    @Test
    void deposit_to_unknown_account_returns_404() {
        var req = new MoneyOperationRequest(new BigDecimal("50.00"), "EUR");
        ResponseEntity<Map<String, Object>> resp = post(BASE + "/" + UUID.randomUUID() + "/deposits", req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody().get("errorCode")).isEqualTo("ACCOUNT_NOT_FOUND");
    }

    @Test
    void create_account_with_blank_name_returns_400() {
        var req = new CreateAccountRequest("", new BigDecimal("100.00"), "EUR");
        ResponseEntity<Map<String, Object>> resp = post(BASE, req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().get("errorCode")).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void withdraw_full_balance_succeeds_leaving_zero() {
        var createReq = new CreateAccountRequest("Carol", new BigDecimal("100.00"), "EUR");
        ResponseEntity<Map<String, Object>> createResp = post(BASE, createReq);
        UUID accountId = UUID.fromString((String) createResp.getBody().get("accountId"));

        var withdrawReq = new MoneyOperationRequest(new BigDecimal("100.00"), "EUR");
        ResponseEntity<Map<String, Object>> resp = post(BASE + "/" + accountId + "/withdrawals", withdrawReq);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("balanceAfter")).isEqualTo(0.0);
    }

    @Test
    void multiple_accounts_are_independent() {
        var req1 = new CreateAccountRequest("Alice", new BigDecimal("100.00"), "EUR");
        var req2 = new CreateAccountRequest("Bob", new BigDecimal("200.00"), "EUR");
        UUID id1 = UUID.fromString((String) post(BASE, req1).getBody().get("accountId"));
        UUID id2 = UUID.fromString((String) post(BASE, req2).getBody().get("accountId"));

        post(BASE + "/" + id1 + "/deposits", new MoneyOperationRequest(new BigDecimal("50.00"), "EUR"));

        ResponseEntity<Map<String, Object>> alice = get(BASE + "/" + id1);
        ResponseEntity<Map<String, Object>> bob = get(BASE + "/" + id2);

        assertThat(alice.getBody().get("balance")).isEqualTo(150.0);
        assertThat(bob.getBody().get("balance")).isEqualTo(200.0);
    }
}
