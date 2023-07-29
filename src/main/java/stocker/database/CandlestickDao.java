package stocker.database;

import stocker.stock.Candlestick;
import stocker.support.StockAppLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static stocker.database.DbConstants.CANDLESTICK_TABLE;
import static stocker.database.DbConstants.DB_URL;
import static stocker.database.DbConstants.DB_USERNAME;
import static stocker.database.DbConstants.DB_PASSWORD;
import static stocker.database.DbConstants.TIME_STAMP_COLUMN;
import static stocker.database.DbConstants.OPEN_COLUMN;
import static stocker.database.DbConstants.CLOSE_COLUMN;
import static stocker.database.DbConstants.LOW_COLUMN;
import static stocker.database.DbConstants.HIGH_COLUMN;
import static stocker.database.DbConstants.VOLUME_COLUMN;

/**
 * Database access object class. Used to interact with the database.
 * @author tribulations
 * @version 1.0
 * @since 1.0
 */
public class CandlestickDao extends Candlestick implements DAO<Candlestick>{

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

    private void setCandleStick(final ResultSet resultSet, final Candlestick candlestick) {
        try {
            candlestick.setTimestamp(resultSet.getLong(TIME_STAMP_COLUMN));
            candlestick.setOpen(resultSet.getDouble(OPEN_COLUMN));
            candlestick.setClose(resultSet.getDouble(CLOSE_COLUMN));
            candlestick.setLow(resultSet.getDouble(LOW_COLUMN));
            candlestick.setHigh(resultSet.getDouble(HIGH_COLUMN));
            candlestick.setVolume(resultSet.getLong(VOLUME_COLUMN));
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
                     "INSERT INTO " + CANDLESTICK_TABLE + " (time_stamp, open, close, low, high, volume, symbol) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?)")
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

    @Override
    public void addRowOverwrite(String symbol, Candlestick candlestick) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO " + CANDLESTICK_TABLE + " (time_stamp, open, close, low, high, volume, symbol) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (time_stamp, symbol) DO UPDATE SET "
                             + "(open, close, low, high, volume) = " +
                             "(excluded.open, excluded.close, excluded.low, excluded.high, excluded.volume)")
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

    @Override
    public void addRowNoOverwrite(String symbol, Candlestick candlestick) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO " + CANDLESTICK_TABLE + " (time_stamp, open, close, low, high, volume, symbol) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING")
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

