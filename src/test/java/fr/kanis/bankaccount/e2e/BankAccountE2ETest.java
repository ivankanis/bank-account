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
import java.util.List;
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

    private Map<String, Object> body(ResponseEntity<Map<String, Object>> resp) {
        var body = resp.getBody();
        assertThat(body).isNotNull();
        return body;
    }

    private UUID createAccount(String owner, String amount) {
        var resp = post(BASE, new CreateAccountRequest(owner, new BigDecimal(amount), "EUR"));
        return UUID.fromString((String) body(resp).get("accountId"));
    }

    @Test
    void full_lifecycle_deposit_withdraw_history() {
        UUID accountId = createAccount("Alice", "100.00");

        // Deposit
        var depositResp = post(BASE + "/" + accountId + "/deposits",
                new MoneyOperationRequest(new BigDecimal("50.00"), "EUR"));
        assertThat(depositResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body(depositResp))
                .containsEntry("type", "DEPOSIT")
                .containsEntry("balanceAfter", 150.0);

        // Withdraw
        var withdrawResp = post(BASE + "/" + accountId + "/withdrawals",
                new MoneyOperationRequest(new BigDecimal("30.00"), "EUR"));
        assertThat(withdrawResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body(withdrawResp))
                .containsEntry("type", "WITHDRAWAL")
                .containsEntry("balanceAfter", 120.0);

        // History
        var historyResp = get(BASE + "/" + accountId + "/transactions");
        assertThat(historyResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<?>) body(historyResp).get("transactions")).hasSize(2);
    }

    @Test
    void withdraw_more_than_balance_returns_422() {
        UUID accountId = createAccount("Bob", "50.00");

        var resp = post(BASE + "/" + accountId + "/withdrawals",
                new MoneyOperationRequest(new BigDecimal("200.00"), "EUR"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(body(resp)).containsEntry("errorCode", "INSUFFICIENT_FUNDS");
    }

    @Test
    void deposit_to_unknown_account_returns_404() {
        var resp = post(BASE + "/" + UUID.randomUUID() + "/deposits",
                new MoneyOperationRequest(new BigDecimal("50.00"), "EUR"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(body(resp)).containsEntry("errorCode", "ACCOUNT_NOT_FOUND");
    }

    @Test
    void create_account_with_blank_name_returns_400() {
        var resp = post(BASE, new CreateAccountRequest("", new BigDecimal("100.00"), "EUR"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(body(resp)).containsEntry("errorCode", "VALIDATION_ERROR");
    }

    @Test
    void withdraw_full_balance_succeeds_leaving_zero() {
        UUID accountId = createAccount("Carol", "100.00");

        var resp = post(BASE + "/" + accountId + "/withdrawals",
                new MoneyOperationRequest(new BigDecimal("100.00"), "EUR"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body(resp)).containsEntry("balanceAfter", 0.0);
    }

    @Test
    void multiple_accounts_are_independent() {
        UUID id1 = createAccount("Alice", "100.00");
        UUID id2 = createAccount("Bob", "200.00");

        post(BASE + "/" + id1 + "/deposits", new MoneyOperationRequest(new BigDecimal("50.00"), "EUR"));

        assertThat(body(get(BASE + "/" + id1))).containsEntry("balance", 150.0);
        assertThat(body(get(BASE + "/" + id2))).containsEntry("balance", 200.0);
    }
}
