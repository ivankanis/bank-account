package fr.kanis.bankaccount.domain.model;

public class AccountNotFoundException extends RuntimeException {

    private final AccountId accountId;

    public AccountNotFoundException(AccountId accountId) {
        super("Account not found: " + accountId.value());
        this.accountId = accountId;
    }

    public AccountId getAccountId() {
        return accountId;
    }
}
