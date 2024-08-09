-- Purpose: This script creates and adds a trigger function to ensure that the table contains only one row per date and symbol.
-- If a row with the same date and symbol already exists, the existing row is updated with the new data.
-- If no such row exists, the new row is inserted.

-- Remember to update schema name and table name in this code when needed.

-- Set the search path to the desired schema
--SET search_path TO test_schema, public;

-- Drop existing function and trigger if they exist
DROP FUNCTION IF EXISTS enforce_unique_date CASCADE;
DROP TRIGGER IF EXISTS enforce_unique_date_trigger ON stock_prices1;

-- Create the trigger function
CREATE OR REPLACE FUNCTION enforce_unique_date()
RETURNS TRIGGER AS $$
BEGIN
    -- Check if the trigger is fired by an INSERT operation
    IF TG_OP = 'INSERT' THEN
        --RAISE NOTICE 'Checking if a row exists with date % and symbol %',
                      --to_char(TO_TIMESTAMP(NEW.timestamp), 'YYYY-MM-DD'), NEW.symbol;
        -- Check if a row with the same date (derived from timestamp) and symbol already exists in the table.
        IF EXISTS (
            SELECT 1
            FROM test_schema.stock_prices1
            WHERE to_char(TO_TIMESTAMP(test_schema.stock_prices1.timestamp), 'YYYY-MM-DD') = to_char(TO_TIMESTAMP(NEW.timestamp), 'YYYY-MM-DD')
            AND test_schema.stock_prices1.symbol = NEW.symbol
        ) THEN
            --RAISE NOTICE 'Updating row with date % and symbol %',
                          --to_char(TO_TIMESTAMP(NEW.timestamp), 'YYYY-MM-DD'), NEW.symbol;

            -- If a row with the same date and symbol exists, update it with the new data.
            UPDATE test_schema.stock_prices1
            SET open = NEW.open,
                high = NEW.high,
                low = NEW.low,
                close = NEW.close,
                volume = NEW.volume,
                timestamp = NEW.timestamp
            WHERE to_char(TO_TIMESTAMP(test_schema.stock_prices1.timestamp), 'YYYY-MM-DD') = to_char(TO_TIMESTAMP(NEW.timestamp), 'YYYY-MM-DD')
            AND test_schema.stock_prices1.symbol = NEW.symbol;

            -- Prevent the insert from proceeding, as the row has been handled by the update statement.
            RETURN NULL;
        END IF;
    END IF;

    -- If no existing row with the same date and symbol is found, proceed with the insert.
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create the trigger
CREATE TRIGGER enforce_unique_date_trigger
-- Specify that this trigger should fire before an INSERT operation on the table.
BEFORE INSERT ON test_schema.stock_prices1
-- Specify that the trigger should be executed for each row affected by the operation.
FOR EACH ROW
-- Specify the function to be executed by this trigger.
EXECUTE FUNCTION enforce_unique_date();
