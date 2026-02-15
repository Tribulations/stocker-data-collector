CREATE TABLE IF NOT EXISTS stock_prices_schema.stock_prices_intraday (
    id BIGSERIAL PRIMARY KEY,
    timestamp BIGINT NOT NULL,
    interval VARCHAR(10) NOT NULL,
    open DECIMAL(15,6) NOT NULL,
    close DECIMAL(15,6) NOT NULL,
    low DECIMAL(15,6) NOT NULL,
    high DECIMAL(15,6) NOT NULL,
    volume BIGINT NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
