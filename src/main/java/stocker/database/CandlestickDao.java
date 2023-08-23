package stocker.database;

import stocker.representation.Candlestick;
import stocker.support.StockAppLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static stocker.database.DbConstants.*;

/**
 * Database access object class. Used to interact with the database.
 * TODO whith this implementation I suppose that a new connection is established each time this object is used?
 * TODO should make a connect method or something??
 * @author tribulations
 * @version 1.0
 * @since 1.0
 */
public class CandlestickDao implements DAO<Candlestick>{

    @Override
    public List<Candlestick> getAllRows() {
        List<Candlestick> candlesticks = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + CANDLESTICK_TABLE);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Candlestick candlestick = new Candlestick();
                setCandleStick(resultSet, candlestick);
                candlesticks.add(candlestick);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return candlesticks;
    }

    @Override
    public List<Candlestick> getAllRowsByName(final String name) {
        List<Candlestick> candlesticks = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + CANDLESTICK_TABLE + " WHERE symbol = ?");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Candlestick candlestick = new Candlestick();
                setCandleStick(resultSet, candlestick);
                candlesticks.add(candlestick);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return candlesticks;
    }

    /**
     * internal method
     * @param resultSet
     * @param candlestick
     */
    private void setCandleStick(final ResultSet resultSet, final Candlestick candlestick) {
        try {
            candlestick.setTimestamp(resultSet.getLong(TIME_STAMP_COLUMN));
            candlestick.setOpen(resultSet.getDouble(OPEN_COLUMN));
            candlestick.setClose(resultSet.getDouble(CLOSE_COLUMN));
            candlestick.setLow(resultSet.getDouble(LOW_COLUMN));
            candlestick.setHigh(resultSet.getDouble(HIGH_COLUMN));
            candlestick.setVolume(resultSet.getLong(VOLUME_COLUMN));
            candlestick.setInterval(resultSet.getString(INTERVAL_COLUMN));
        } catch (SQLException e) {
            StockAppLogger.INSTANCE.logInfo(e.getMessage());
            e.printStackTrace();
        }
    }

//    @Override
//    public List<Candlestick> getRowsByName(final String name) {
//        Candlestick candlestick = null;
//
//        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
//             PreparedStatement statement = connection.prepareStatement(
//                     "SELECT * FROM " + TABLE + " WHERE id = ?");
//        ) {
//            statement.setInt(1, id);
//            try (ResultSet resultSet = statement.executeQuery()) {
//                if (resultSet.next()) {
//                    candlestick = new Candlestick();
//                    candlestick.setId(resultSet.getInt("id"));
//                    candlestick.setName(resultSet.getString("name"));
//                    candlestick.setAge(resultSet.getInt("age"));
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        return candlestick;
//    }

    @Override
    public void addRow(String symbol, Candlestick candlestick) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO " + CANDLESTICK_TABLE
                             + " (time_stamp, open, close, low, high, volume, symbol, interval) "
                             + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
        ) {
            statement.setLong(1, candlestick.getTimestamp());
            statement.setDouble(2, candlestick.getOpen());
            statement.setDouble(3, candlestick.getClose());
            statement.setDouble(4, candlestick.getLow());
            statement.setDouble(5, candlestick.getHigh());
            statement.setDouble(6, candlestick.getVolume());
            statement.setString(7, symbol);
            statement.setString(8, candlestick.getInterval());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getDbConnection() {
        try {
            return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        } catch (SQLException e) {
            StockAppLogger.INSTANCE.logInfo(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * used to add a single price data row to the database. TODO should add functiolaity to check if the data being added is in the same day when we fetch 1 day interval and update so the database only contains the latest candlestick timestampdata? Or only fetch 1d data once each day? Have one method for each type of interval when adding to db?
     * @param symbol
     * @param candlesticks
     */
    @Override
    public void addRows(String symbol, List<Candlestick> candlesticks) {
        try {
            Connection connection = getDbConnection();

            for (Candlestick candlestick : candlesticks) {
                if (connection != null) {
                    PreparedStatement statement = connection.prepareStatement(
                            "INSERT INTO " + CANDLESTICK_TABLE
                                    + " (time_stamp, open, close, low, high, volume, symbol, interval) "
                                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                    // when fetching multiple 1d candles the timestamp is set to 9:00 am so we increase it to be 17:39
                    if (candlestick.getInterval().equals("1d")) {
                        statement.setLong(1, candlestick.getTimestamp());
                    } else {
                        statement.setLong(1, candlestick.getTimestamp());
                    }
                    statement.setDouble(2, candlestick.getOpen());
                    statement.setDouble(3, candlestick.getClose());
                    statement.setDouble(4, candlestick.getLow());
                    statement.setDouble(5, candlestick.getHigh());
                    statement.setDouble(6, candlestick.getVolume());
                    statement.setString(7, symbol);
                    statement.setString(8, candlestick.getInterval());
                    statement.executeUpdate();
                } else {
                    StockAppLogger.INSTANCE.logInfo("no db connection: connection is null");
                }
            }
            if (connection != null) { // TODO imporve
                connection.close();
            }
        } catch (SQLException e) {
            StockAppLogger.INSTANCE.logInfo(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Adds one row to the database overwriting the open, close, low, high and volume if the timestamp
     * already exists for the current symbol, i.e. if the symbol AAB with datetime 2023-08-01 17:30 already exists
     * the open, close, low, high and volume will be updated for this symbol and datetime/timestamp.
     * @param symbol the stock name/symbol
     * @param candlestick the candlestick containing the price data for the symbol which should be added to th database
     */
    @Override
    public void addRowOverwrite(String symbol, Candlestick candlestick) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO " + CANDLESTICK_TABLE
                             + " (time_stamp, open, close, low, high, volume, symbol, interval) "
                             + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (time_stamp, symbol, interval) DO UPDATE SET "
                             + "(open, close, low, high, volume) = "
                             + "(excluded.open, excluded.close, excluded.low, excluded.high, excluded.volume)")
        ) {
            statement.setLong(1, candlestick.getTimestamp());
            statement.setDouble(2, candlestick.getOpen());
            statement.setDouble(3, candlestick.getClose());
            statement.setDouble(4, candlestick.getLow());
            statement.setDouble(5, candlestick.getHigh());
            statement.setDouble(6, candlestick.getVolume());
            statement.setString(7, symbol);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds one row to the database if the timestamp for the current symbol do ot already exist in the database.
     * @param symbol the stock name/symbol
     * @param candlestick the candlestick containing the price data for the symbol which should be added to th database
     */
    @Override
    public void addRowNoOverwrite(String symbol, Candlestick candlestick) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO " + CANDLESTICK_TABLE
                             + " (time_stamp, open, close, low, high, volume, symbol, interval) "
                             + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING")
        ) {
            statement.setLong(1, candlestick.getTimestamp());
            statement.setDouble(2, candlestick.getOpen());
            statement.setDouble(3, candlestick.getClose());
            statement.setDouble(4, candlestick.getLow());
            statement.setDouble(5, candlestick.getHigh());
            statement.setDouble(6, candlestick.getVolume());
            statement.setString(7, symbol);
            statement.setString(8, candlestick.getInterval());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void updateRow(Candlestick candlestick) {
//        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
//             PreparedStatement statement = connection.prepareStatement(
//                     "UPDATE " + TABLE + " SET name = ? WHERE id = ?")
//        ) {
//            statement.setString(1, candlestick.getName());
//            statement.setInt(2, candlestick.getId());
//            statement.executeUpdate();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

//    @Override
//    public void deleteById(int id) {
//        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
//             PreparedStatement statement = connection.prepareStatement(
//                     "DELETE FROM " + TABLE + " WHERE id = ?")
//        ) {
//            statement.setInt(1, id);
//            statement.executeUpdate();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
}

