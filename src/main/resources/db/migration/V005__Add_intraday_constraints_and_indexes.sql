ALTER TABLE stock_prices_schema.stock_prices_intraday
ADD CONSTRAINT unique_timestamp_symbol_interval
UNIQUE (timestamp, symbol, interval);

CREATE INDEX IF NOT EXISTS idx_intraday_symbol_interval_timestamp
ON stock_prices_schema.stock_prices_intraday(symbol, interval, timestamp);

CREATE INDEX IF NOT EXISTS idx_intraday_symbol
ON stock_prices_schema.stock_prices_intraday(symbol);

CREATE INDEX IF NOT EXISTS idx_intraday_interval
ON stock_prices_schema.stock_prices_intraday(interval);

CREATE INDEX IF NOT EXISTS idx_intraday_timestamp
ON stock_prices_schema.stock_prices_intraday(timestamp);
