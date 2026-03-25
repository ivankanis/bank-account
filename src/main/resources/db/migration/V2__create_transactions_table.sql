CREATE TABLE transactions (
    id           UUID          NOT NULL,
    account_id   UUID          NOT NULL,
    type         VARCHAR(20)   NOT NULL,
    amount       NUMERIC(19,2) NOT NULL,
    currency     VARCHAR(3)    NOT NULL,
    balance_after NUMERIC(19,2) NOT NULL,
    occurred_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_transactions PRIMARY KEY (id),
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts(id)
);

CREATE INDEX idx_transactions_account_id ON transactions(account_id);
