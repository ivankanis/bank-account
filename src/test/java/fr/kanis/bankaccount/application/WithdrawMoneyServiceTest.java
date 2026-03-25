package fr.kanis.bankaccount.application;

import fr.kanis.bankaccount.application.command.WithdrawMoneyCommand;
import fr.kanis.bankaccount.application.service.WithdrawMoneyService;
import fr.kanis.bankaccount.domain.model.*;
import fr.kanis.bankaccount.domain.port.out.AccountRepository;
import fr.kanis.bankaccount.domain.port.out.TransactionRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WithdrawMoneyServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private WithdrawMoneyService service;

    @BeforeEach
    void setUp() {
        service = new WithdrawMoneyService(accountRepository, transactionRepository);
    }

    @Test
    void withdraw_loads_account_updates_and_persists() {
        AccountId accountId = AccountId.generate();
        Account account = Account.reconstitute(accountId, "Bob", Money.of("200.00", "EUR"), List.of(), Instant.now());
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);
        Transaction savedTx = Transaction.withdrawal(Money.of("50.00", "EUR"), Money.of("150.00", "EUR"), Instant.now());
        when(transactionRepository.save(any(), eq(accountId))).thenReturn(savedTx);

        var command = new WithdrawMoneyCommand(accountId, new BigDecimal("50.00"), "EUR");
        Transaction result = service.withdraw(command);

        assertThat(result.type()).isEqualTo(TransactionType.WITHDRAWAL);
        verify(accountRepository).save(account);
    }

    @Test
    void withdraw_does_not_persist_when_insufficient_funds() {
        AccountId accountId = AccountId.generate();
        Account account = Account.reconstitute(accountId, "Bob", Money.of("10.00", "EUR"), List.of(), Instant.now());
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        var command = new WithdrawMoneyCommand(accountId, new BigDecimal("100.00"), "EUR");

        assertThatExceptionOfType(InsufficientFundsException.class)
                .isThrownBy(() -> service.withdraw(command));

        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any(), any());
    }

    @Test
    void withdraw_throws_when_account_not_found() {
        AccountId unknownId = AccountId.generate();
        when(accountRepository.findById(unknownId)).thenReturn(Optional.empty());

        var command = new WithdrawMoneyCommand(unknownId, new BigDecimal("50.00"), "EUR");

        assertThatExceptionOfType(AccountNotFoundException.class)
                .isThrownBy(() -> service.withdraw(command));
    }
}
