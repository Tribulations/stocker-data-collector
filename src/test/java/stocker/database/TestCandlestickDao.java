package stocker.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A test-specific subclass of CandlestickDao that allows injecting a mock connection.
 * This class is used for testing purposes only.
 */
public class TestCandlestickDao extends CandlestickDao {
    
    private final Connection mockConnection;
    
    /**
     * Creates a new TestCandlestickDao with the provided mock connection.
     * 
     * @param mockConnection the mock connection to use for database operations
     */
    public TestCandlestickDao(Connection mockConnection) {
        this.mockConnection = mockConnection;
    }
    
    /**
     * Returns the mock connection instead of creating a new one.
     * This method is used to override the behavior of the parent class.
     * 
     * @return the mock connection
     */
    @Override
    protected Connection getDbConnection() throws SQLException {
        return mockConnection;
    }
}
