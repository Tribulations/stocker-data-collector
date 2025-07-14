package stocker.database;

import java.util.List;

public interface DAO<T> {
    List<T> getAllRows();
    List<T> getAllRowsByName(final String name);
    void addRow(String symbol, T row);
    void addRows(String symbol, List<T> rows);
    void resetTable();
}
