-- Create stock prices table
CREATE TABLE IF NOT EXISTS stock_prices_schema.stock_prices_1day (
    id BIGSERIAL PRIMARY KEY,
    timestamp BIGINT NOT NULL,
    open DECIMAL(15,6) NOT NULL,
    close DECIMAL(15,6) NOT NULL,
    low DECIMAL(15,6) NOT NULL,
    high DECIMAL(15,6) NOT NULL,
    volume BIGINT NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Comments for documentation
COMMENT ON TABLE stock_prices_schema.stock_prices_1day IS '1-day Stock price candlestick data';
COMMENT ON COLUMN stock_prices_schema.stock_prices_1day.timestamp IS 'Unix timestamp of the candlestick period';
COMMENT ON COLUMN stock_prices_schema.stock_prices_1day.open IS 'Opening price';
COMMENT ON COLUMN stock_prices_schema.stock_prices_1day.close IS 'Closing price';
COMMENT ON COLUMN stock_prices_schema.stock_prices_1day.low IS 'Lowest price during period';
COMMENT ON COLUMN stock_prices_schema.stock_prices_1day.high IS 'Highest price during period';
COMMENT ON COLUMN stock_prices_schema.stock_prices_1day.volume IS 'Trading volume';
COMMENT ON COLUMN stock_prices_schema.stock_prices_1day.symbol IS 'Stock symbol (e.g., BOL.ST, ABB.ST)';

-- Log successful creation
DO $$
BEGIN
    RAISE NOTICE 'Stock prices 1 day table created successfully in stock_prices_schema';
END $$;