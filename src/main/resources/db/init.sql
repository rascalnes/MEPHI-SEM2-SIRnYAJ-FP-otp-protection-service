-- Создание таблицы пользователей
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    login VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'USER')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Создание таблицы конфигурации OTP (всегда 1 запись)
CREATE TABLE IF NOT EXISTS otp_config (
    id INTEGER PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    lifetime_seconds INTEGER NOT NULL DEFAULT 300,
    code_length INTEGER NOT NULL DEFAULT 6,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

-- Создание таблицы OTP-кодов
CREATE TABLE IF NOT EXISTS otp_codes (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    operation_id VARCHAR(255) NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'EXPIRED', 'USED')),
    channel VARCHAR(20) NOT NULL CHECK (channel IN ('SMS', 'EMAIL', 'TELEGRAM', 'FILE')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    validated_at TIMESTAMP
);

-- Индексы для производительности
CREATE INDEX idx_otp_codes_operation_id ON otp_codes(operation_id);
CREATE INDEX idx_otp_codes_status_expires ON otp_codes(status, expires_at);
CREATE INDEX idx_users_login ON users(login);

-- Вставка начальной конфигурации (если пусто)
INSERT INTO otp_config (id, lifetime_seconds, code_length, updated_by)
SELECT 1, 300, 6, 'system'
WHERE NOT EXISTS (SELECT 1 FROM otp_config);