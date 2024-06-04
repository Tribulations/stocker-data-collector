# Stocker

An application providing different functionality to facilitate finding stock trading opportunities.

- Connect to postgre db with terminal: psql -h <ip of host> -U <database username> -d <database name> e.g. psql -h 155.4.55.36 -U jocka -d test_db

- Database is stored on the Ubuntu v-rum computer. Use ssh to conveniently connect to this computer remotely. 

- The file /etc/postgresql/14/main/pg_hba.conf is used to configure which connections that are allowed to connect to the database?
  After modifying the pg_hba.conf file, you need to reload the PostgreSQL server configuration for the changes to take effect. You can do this by running the following command:
> sudo systemctl reload postgresql
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
- Display schemas in selected database
> \dn
- Alternatively, you can use the following SQL query to retrieve the schema names:
````sql
SELECT nspname FROM pg_catalog.pg_namespace;
````
### Checking table columns etc.
- To show the column names and data types of a table in PostgreSQL, you can use the \d command in the psql terminal, followed by the table name.
  For example, if you have a table named users, you can use the following command:
> \d users
- Alternatively, you can use the SQL DESCRIBE command, which provides a more concise output:
````sql
DESCRIBE users;
````

### Create new price data table examples

- Generic example
````sql
CREATE TABLE table_name (
column1 data_type1 constraints1,
column2 data_type2 constraints2,
...,
CONSTRAINT constraint_name PRIMARY KEY (column1, column2, ...)
);
````

- Concrete example: Creating a table to store stock price data
````sql
CREATE TABLE stock_prices (
    timestamp BIGINT NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    open REAL NOT NULL,
    high REAL NOT NULL,
    low REAL NOT NULL,
    close REAL NOT NULL,
    volume BIGINT NOT NULL,
    CONSTRAINT pk_stock_prices PRIMARY KEY (timestamp, symbol)
);
````

## other useful info
- database name is stored in database.DbConstants.java