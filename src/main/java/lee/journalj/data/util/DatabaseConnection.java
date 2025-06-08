package lee.journalj.data.util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Класс для управления подключениями к базе данных.
 */
public class DatabaseConnection {
    private final Connection connection;

    public DatabaseConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
} 