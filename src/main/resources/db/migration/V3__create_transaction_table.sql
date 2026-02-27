
CREATE TABLE tb_transactions (
    id UUID PRIMARY KEY,

    transaction_type VARCHAR(20) NOT NULL
        CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER')),

    amount NUMERIC(15,2) NOT NULL CHECK (amount >= 0),

    description VARCHAR(255),

    source_account_id UUID,
    destination_account_id UUID,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_source_account
        FOREIGN KEY (source_account_id)
        REFERENCES tb_accounts(id),

    CONSTRAINT fk_destination_account
        FOREIGN KEY (destination_account_id)
        REFERENCES tb_accounts(id)
);

