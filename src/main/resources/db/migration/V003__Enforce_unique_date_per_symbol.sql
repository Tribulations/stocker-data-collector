-- Purpose: Ensure only one row per symbol per day in stock_prices_1day. If a row with the same date and symbol exists, update it instead of inserting a new one.

-- Drop existing function and trigger if they exist
DROP FUNCTION IF EXISTS enforce_unique_date_per_symbol CASCADE;
DROP TRIGGER IF EXISTS enforce_unique_date_per_symbol_trigger ON stock_prices_schema.stock_prices_1day;

-- Create the trigger function
CREATE OR REPLACE FUNCTION enforce_unique_date_per_symbol()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        IF EXISTS (
            SELECT 1
            FROM stock_prices_schema.stock_prices_1day
            WHERE to_char(TO_TIMESTAMP(stock_prices_schema.stock_prices_1day.timestamp), 'YYYY-MM-DD') = to_char(TO_TIMESTAMP(NEW.timestamp), 'YYYY-MM-DD')
              AND stock_prices_schema.stock_prices_1day.symbol = NEW.symbol
        ) THEN
            UPDATE stock_prices_schema.stock_prices_1day
            SET open = NEW.open,
                high = NEW.high,
                low = NEW.low,
                close = NEW.close,
                volume = NEW.volume,
                timestamp = NEW.timestamp,
                updated_at = CURRENT_TIMESTAMP
            WHERE to_char(TO_TIMESTAMP(stock_prices_schema.stock_prices_1day.timestamp), 'YYYY-MM-DD') = to_char(TO_TIMESTAMP(NEW.timestamp), 'YYYY-MM-DD')
              AND stock_prices_schema.stock_prices_1day.symbol = NEW.symbol;
            RETURN NULL;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create the trigger
CREATE TRIGGER enforce_unique_date_per_symbol_trigger
BEFORE INSERT ON stock_prices_schema.stock_prices_1day
FOR EACH ROW
EXECUTE FUNCTION enforce_unique_date_per_symbol();
