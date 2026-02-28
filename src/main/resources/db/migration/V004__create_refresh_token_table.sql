
CREATE TABLE tb_refresh_tokens (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL UNIQUE,
    token           VARCHAR(512) NOT NULL UNIQUE,
    revoked         BOOLEAN NOT NULL DEFAULT FALSE,
    expiry_date     TIMESTAMP NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
        REFERENCES tb_users(id)
        ON DELETE CASCADE
);

-- Índices opcionais (melhoram performance em buscas frequentes)
CREATE INDEX idx_refresh_token_user_id ON tb_refresh_tokens(user_id);
CREATE INDEX idx_refresh_token_expiry_date ON tb_refresh_tokens(expiry_date);