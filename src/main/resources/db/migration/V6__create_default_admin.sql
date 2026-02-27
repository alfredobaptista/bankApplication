-- V6__create_default_admin.sql
INSERT INTO tb_users (
    id,
    name,
    bi,
    email,
    password,
    phone_number,
    role,
    created_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440000',  -- UUID fixo (podes gerar um novo com uuidgenerator.net)
    'Alfredo Baptista',
    '000000000UE00',
    'admin@bank.com',
    '$2a$10$iVTIhkw4QLJZjZCmJ/lNke/.gI72N7VABw5mqsQKnnQkqrABXD/Eu',
    '+244951893360',
    'ROLE_ADMIN',
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;