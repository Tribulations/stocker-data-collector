package stocker.database;

import stocker.datafetchers.wJson.Candlestick;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CandlestickDao extends Candlestick implements DAO<Candlestick>{
    private static final String DB_URL = "jdbc:postgresql://155.4.55.36:5432/test_db";
    private static final String DB_USERNAME = "jocka";
    private static final String DB_PASSWORD = "jocka123";
    private static final String TABLE = "test_schema.temp_price";

    @Override
    public List<Candlestick> getAllRows() {
        List<Candlestick> candlesticks = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + TABLE);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Candlestick candlestick = new Candlestick();
                candlestick.setTimestamp(resultSet.getLong("time_stamp"));
                candlestick.setOpen(resultSet.getDouble("open"));
                candlestick.setClose(resultSet.getDouble("close"));
                candlestick.setLow(resultSet.getDouble("low"));
                candlestick.setHigh(resultSet.getDouble("high"));
                candlestick.setVolume(resultSet.getLong("volume"));
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
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + TABLE + " WHERE symbol = ?");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Candlestick candlestick = new Candlestick();
                candlestick.setTimestamp(resultSet.getLong("time_stamp"));
                candlestick.setOpen(resultSet.getDouble("open"));
                candlestick.setClose(resultSet.getDouble("close"));
                candlestick.setLow(resultSet.getDouble("low"));
                candlestick.setHigh(resultSet.getDouble("high"));
                candlestick.setVolume(resultSet.getLong("volume"));
                candlesticks.add(candlestick);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return candlesticks;
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
                     "INSERT INTO " + TABLE + " (time_stamp, open, close, low, high, volume, symbol) " +
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
                     "INSERT INTO " + TABLE + " (time_stamp, open, close, low, high, volume, symbol) " +
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
                     "INSERT INTO " + TABLE + " (time_stamp, open, close, low, high, volume, symbol) " +
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

