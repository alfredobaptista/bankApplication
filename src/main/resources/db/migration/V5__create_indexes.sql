CREATE INDEX idx_accounts_number ON tb_accounts(account_number);

CREATE INDEX idx_user_bi ON tb_users(bi);

CREATE INDEX idx_user_id ON tb_refresh_tokens(user_id);

CREATE INDEX idx_token ON tb_refresh_tokens(token);