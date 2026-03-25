CREATE TABLE accounts (
    id         UUID         NOT NULL,
    owner_name VARCHAR(255) NOT NULL,
    balance    NUMERIC(19,2) NOT NULL,
    currency   VARCHAR(3)   NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_accounts PRIMARY KEY (id)
);
