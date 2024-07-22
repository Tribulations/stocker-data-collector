# TODO

- refactor CandlestickDao

## Fetching data and adding to db

### The following steps are done for each stock.
1. fetch all new data at the end of each trading day (closes at 17.30)
> 1.2.   

2. Add all newly fetched data to the database


# TODO 
    create table for one day candlesticks
    add new candle everyday after close
    For one day price data we only care about its date. Its time is not important.
    We should be able to always overwrite a price data if it already exists in the one day table as everytime we fetch the most recently fetched candle should be the latest    one! And in this case, it is valid to just overwrite any price data with the same date! 
