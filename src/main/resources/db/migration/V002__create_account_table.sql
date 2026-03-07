CREATE TABLE tb_accounts (
    id UUID PRIMARY KEY,
    account_number  VARCHAR(15) NOT NULL UNIQUE,
    ledger_balance  NUMERIC(15,2) NOT NULL DEFAULT 0.00 CHECK (ledger_balance >= 0),
    available_balance NUMERIC(15,2) NOT NULL DEFAULT 0.00 CHECK (available_balance >= -5000.00),
    currency_code   VARCHAR(3) NOT NULL DEFAULT 'AOA',
    account_type    VARCHAR(20) NOT NULL CHECK (account_type IN ('CHECKING', 'SAVINGS', 'BUSINESS', 'FIXED_DEPOSIT')),
    status          VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'BLOCKED', 'CLOSED', 'DORMANT', 'FROZEN')),
    user_id         UUID NOT NULL UNIQUE,
    version         BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user
            FOREIGN KEY (user_id)
            REFERENCES tb_users(id)
            ON DELETE CASCADE
);



CREATE SEQUENCE seq_account_number
    START WITH 100000
    INCREMENT BY 1
    MAXVALUE 999999;
