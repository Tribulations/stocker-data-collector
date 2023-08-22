# Stocker
An application providing different functionality to facilitate finding stock trading opportunities.

- Connect to postgre db with terminal: psql -h <ip of host> -U <database username> -d <database name> e.g. psql -h 155.4.55.36 -U jocka -d test_db

## psql terminal commands:
- Set the search path to the schema in order to query without specifying the schema
> SET search_path TO test_schema;
- query stock price data ordered by the latest prices first
> SELECT to_char(to_timestamp(time_stamp), 'YYYY-MM-DD HH24:MI'), open, close, low, high, volume, symbol, interval FROM temp_price3 ORDER BY time_stamp DESC;
- quit the psql terminal 
> \q
- show relations/tables in schema
> \dt
- display help
> \h or \?
- create new price data table example:
> CREATE TABLE temp_price3(                                                                                               time_stamp BIGINT NOT NULL,
open REAL NOT NULL,
close REAL NOT NULL,
low REAL NOT NULL,
high REAL NOT NULL,
volume BIGINT NOT NULL,
symbol VARCHAR(10) NOT NULL, interval VARCHAR(5) NOT NULL,
CONSTRAINT pk_temp_price3 PRIMARY KEY(time_stamp, symbol, interval));

## other useful info
- database name is stored in database.DbConstants.java