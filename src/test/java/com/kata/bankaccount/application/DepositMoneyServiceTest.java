package com.kata.bankaccount.application;

import com.kata.bankaccount.application.command.DepositMoneyCommand;
import com.kata.bankaccount.application.service.DepositMoneyService;
import com.kata.bankaccount.domain.model.*;
import com.kata.bankaccount.domain.port.out.AccountRepository;
import com.kata.bankaccount.domain.port.out.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositMoneyServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private DepositMoneyService service;

    @BeforeEach
    void setUp() {
        service = new DepositMoneyService(accountRepository, transactionRepository);
    }

    @Test
    void deposit_loads_account_updates_and_persists() {
        Account account = Account.reconstitute(
                AccountId.generate(),
                "Alice",
                Money.of("100.00", "EUR"),
                List.of(),
                Instant.now()
        );
        when(accountRepository.findById(account.id())).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);
        Transaction savedTx = Transaction.deposit(Money.of("50.00", "EUR"), Money.of("150.00", "EUR"), Instant.now());
        when(transactionRepository.save(any(), eq(account.id()))).thenReturn(savedTx);

        var command = new DepositMoneyCommand(account.id(), new BigDecimal("50.00"), "EUR");
        Transaction result = service.deposit(command);

        assertThat(result.type()).isEqualTo(TransactionType.DEPOSIT);
        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(Transaction.class), eq(account.id()));
    }

    @Test
    void deposit_throws_when_account_not_found() {
        AccountId unknownId = AccountId.generate();
        when(accountRepository.findById(unknownId)).thenReturn(Optional.empty());

        var command = new DepositMoneyCommand(unknownId, new BigDecimal("50.00"), "EUR");

        assertThatExceptionOfType(AccountNotFoundException.class)
                .isThrownBy(() -> service.deposit(command));

        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any(), any());
    }
}
