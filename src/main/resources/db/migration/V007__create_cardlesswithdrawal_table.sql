CREATE TABLE tb_cardless_with_drawal (
    id  BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    account_number  VARCHAR(15) NOT NULL,
    amount          NUMERIC(15,2) NOT NULL CHECK (amount > 0),
    reference_code  VARCHAR(12) NOT NULL UNIQUE,
    secret_code     VARCHAR(6) NOT NULL,
    expiry          TIMESTAMP NOT NULL,
    status          VARCHAR(20) NOT NULL  DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'EXPIRED', 'CANCELLED')),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    CONSTRAINT uq_reference_code UNIQUE (reference_code)
);
