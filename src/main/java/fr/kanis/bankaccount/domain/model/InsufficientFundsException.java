package fr.kanis.bankaccount.domain.model;

public class InsufficientFundsException extends RuntimeException {

    private final AccountId accountId;
    private final Money currentBalance;
    private final Money requested;

    public InsufficientFundsException(AccountId accountId, Money currentBalance, Money requested) {
        super("Account %s has insufficient funds: balance=%s, requested=%s"
                .formatted(accountId.value(), currentBalance.amount(), requested.amount()));
        this.accountId = accountId;
        this.currentBalance = currentBalance;
        this.requested = requested;
    }

    public AccountId getAccountId() {
        return accountId;
    }

    public Money getCurrentBalance() {
        return currentBalance;
    }

    public Money getRequested() {
        return requested;
    }
}
