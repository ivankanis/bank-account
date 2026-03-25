package com.kata.bankaccount.infrastructure.config;

import com.kata.bankaccount.application.service.*;
import com.kata.bankaccount.domain.port.in.*;
import com.kata.bankaccount.domain.port.out.AccountRepository;
import com.kata.bankaccount.domain.port.out.TransactionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public CreateAccountUseCase createAccountUseCase(AccountRepository accountRepository) {
        return new CreateAccountService(accountRepository);
    }

    @Bean
    public DepositMoneyUseCase depositMoneyUseCase(AccountRepository accountRepository,
                                                   TransactionRepository transactionRepository) {
        return new DepositMoneyService(accountRepository, transactionRepository);
    }

    @Bean
    public WithdrawMoneyUseCase withdrawMoneyUseCase(AccountRepository accountRepository,
                                                     TransactionRepository transactionRepository) {
        return new WithdrawMoneyService(accountRepository, transactionRepository);
    }

    @Bean
    public GetTransactionHistoryUseCase getTransactionHistoryUseCase(AccountRepository accountRepository,
                                                                     TransactionRepository transactionRepository) {
        return new GetTransactionHistoryService(accountRepository, transactionRepository);
    }

    @Bean
    public GetAccountUseCase getAccountUseCase(AccountRepository accountRepository) {
        return new GetAccountService(accountRepository);
    }
}
