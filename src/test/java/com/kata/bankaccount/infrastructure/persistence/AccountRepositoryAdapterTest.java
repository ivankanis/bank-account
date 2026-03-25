package com.kata.bankaccount.infrastructure.persistence;

import com.kata.bankaccount.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({AccountRepositoryAdapter.class, TransactionRepositoryAdapter.class})
class AccountRepositoryAdapterTest {

    @Autowired
    private AccountRepositoryAdapter adapter;

    @Test
    void save_and_find_by_id_roundtrip() {
        Account account = Account.open("Alice", Money.of("100.00", "EUR"), Instant.now());
        adapter.save(account);

        Optional<Account> found = adapter.findById(account.id());

        assertThat(found).isPresent();
        assertThat(found.get().ownerName()).isEqualTo("Alice");
        assertThat(found.get().balance()).isEqualTo(Money.of("100.00", "EUR"));
        assertThat(found.get().transactions()).isEmpty();
    }

    @Test
    void find_by_unknown_id_returns_empty() {
        Optional<Account> found = adapter.findById(AccountId.generate());
        assertThat(found).isEmpty();
    }

    @Test
    void save_updates_balance_on_second_save() {
        Account account = Account.open("Bob", Money.of("200.00", "EUR"), Instant.now());
        adapter.save(account);

        account.deposit(Money.of("50.00", "EUR"), Instant.now());
        adapter.save(account);

        Optional<Account> found = adapter.findById(account.id());
        assertThat(found).isPresent();
        assertThat(found.get().balance()).isEqualTo(Money.of("250.00", "EUR"));
    }

    @Test
    void find_all_returns_all_saved_accounts() {
        Account a1 = Account.open("Alice", Money.of("100.00", "EUR"), Instant.now());
        Account a2 = Account.open("Bob", Money.of("200.00", "EUR"), Instant.now());
        adapter.save(a1);
        adapter.save(a2);

        List<Account> all = adapter.findAll();
        assertThat(all).hasSize(2);
    }
}
