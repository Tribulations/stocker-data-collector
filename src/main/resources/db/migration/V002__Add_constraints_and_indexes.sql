-- Unique constraint for upsert ON CONFLICT
ALTER TABLE stock_prices_schema.stock_prices_1day
ADD CONSTRAINT unique_timestamp_symbol
UNIQUE (timestamp, symbol);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_stock_prices_symbol_timestamp
ON stock_prices_schema.stock_prices_1day(symbol, timestamp);

CREATE INDEX IF NOT EXISTS idx_stock_prices_symbol
ON stock_prices_schema.stock_prices_1day(symbol);

CREATE INDEX IF NOT EXISTS idx_stock_prices_timestamp
ON stock_prices_schema.stock_prices_1day(timestamp);

-- NO check constraints - Java validation handles data integrity to maximize upsert performance

SELECT 'Database optimized for Java-validated upserts with explicit schema' as status;