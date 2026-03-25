package fr.kanis.bankaccount.domain;

import fr.kanis.bankaccount.domain.model.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class AccountTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
    private static final Money INITIAL = Money.of("100.00", "EUR");

    @Test
    void open_creates_account_with_initial_balance_and_empty_history() {
        Account account = Account.open("Alice", INITIAL, NOW);

        assertThat(account.ownerName()).isEqualTo("Alice");
        assertThat(account.balance()).isEqualTo(INITIAL);
        assertThat(account.transactions()).isEmpty();
        assertThat(account.createdAt()).isEqualTo(NOW);
        assertThat(account.id()).isNotNull();
    }

    @Test
    void open_rejects_blank_owner_name() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Account.open("  ", INITIAL, NOW));
    }

    @Test
    void open_rejects_null_owner_name() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Account.open(null, INITIAL, NOW));
    }

    @Test
    void deposit_increases_balance_and_records_transaction() {
        Account account = Account.open("Alice", INITIAL, NOW);
        Money depositAmount = Money.of("50.00", "EUR");

        Transaction tx = account.deposit(depositAmount, NOW);

        assertThat(account.balance()).isEqualTo(Money.of("150.00", "EUR"));
        assertThat(account.transactions()).hasSize(1);
        assertThat(tx.type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(tx.amount()).isEqualTo(depositAmount);
        assertThat(tx.balanceAfter()).isEqualTo(Money.of("150.00", "EUR"));
    }

    @Test
    void deposit_zero_amount_throws() {
        Account account = Account.open("Alice", INITIAL, NOW);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> account.deposit(Money.of("0.00", "EUR"), NOW));
    }

    @Test
    void withdraw_decreases_balance_and_records_transaction() {
        Account account = Account.open("Alice", INITIAL, NOW);
        Money withdrawAmount = Money.of("30.00", "EUR");

        Transaction tx = account.withdraw(withdrawAmount, NOW);

        assertThat(account.balance()).isEqualTo(Money.of("70.00", "EUR"));
        assertThat(account.transactions()).hasSize(1);
        assertThat(tx.type()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(tx.amount()).isEqualTo(withdrawAmount);
        assertThat(tx.balanceAfter()).isEqualTo(Money.of("70.00", "EUR"));
    }

    @Test
    void withdraw_exact_balance_succeeds() {
        Account account = Account.open("Alice", INITIAL, NOW);

        account.withdraw(INITIAL, NOW);

        assertThat(account.balance()).isEqualTo(Money.of("0.00", "EUR"));
    }

    @Test
    void withdraw_more_than_balance_throws_insufficient_funds() {
        Account account = Account.open("Alice", INITIAL, NOW);

        assertThatExceptionOfType(InsufficientFundsException.class)
                .isThrownBy(() -> account.withdraw(Money.of("200.00", "EUR"), NOW))
                .satisfies(ex -> {
                    assertThat(ex.getCurrentBalance()).isEqualTo(INITIAL);
                    assertThat(ex.getRequested()).isEqualTo(Money.of("200.00", "EUR"));
                });
    }

    @Test
    void multiple_operations_are_all_recorded_in_order() {
        Account account = Account.open("Alice", INITIAL, NOW);

        account.deposit(Money.of("50.00", "EUR"), NOW);
        account.withdraw(Money.of("30.00", "EUR"), NOW);
        account.deposit(Money.of("20.00", "EUR"), NOW);

        assertThat(account.transactions()).hasSize(3);
        assertThat(account.transactions().get(0).type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(account.transactions().get(1).type()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(account.transactions().get(2).type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(account.balance()).isEqualTo(Money.of("140.00", "EUR"));
    }

    @Test
    void transactions_list_is_unmodifiable() {
        Account account = Account.open("Alice", INITIAL, NOW);
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> account.transactions().add(null));
    }
}
