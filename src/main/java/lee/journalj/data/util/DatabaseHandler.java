package lee.journalj.data.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Класс для управления подключениями к базе данных.
 */
public class DatabaseHandler {
    private static DatabaseHandler instance;
    private static Connection connection;
    private static DatabaseConfig config;

    private DatabaseHandler() {}

    public static DatabaseHandler getInstance() {
        if (instance == null) {
            instance = new DatabaseHandler();
        }
        return instance;
    }

    public static class DatabaseConfig {
        private final DatabaseType type;
        private final String url;
        private final String username;
        private final String password;

        public DatabaseConfig(DatabaseType type, String url, String username, String password) {
            this.type = type;
            this.url = url;
            this.username = username;
            this.password = password;
        }

        public String getUrl() {
            return url;
        }

        public String getUser() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

    public enum DatabaseType {
        SQLITE,
        MYSQL,
        POSTGRESQL
    }

    public static void init(DatabaseConfig config) throws SQLException {
        DatabaseHandler.config = config;
        connection = createConnection();
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = createConnection();
        }
        return connection;
    }

    public DatabaseConfig getConfig() {
        return config;
    }

    private static Connection createConnection() throws SQLException {
        if (config == null) {
            throw new SQLException("Database configuration is not initialized");
        }

        switch (config.type) {
            case SQLITE:
                return DriverManager.getConnection(config.url);
            case MYSQL:
            case POSTGRESQL:
                return DriverManager.getConnection(config.url, config.username, config.password);
            default:
                throw new SQLException("Unsupported database type: " + config.type);
        }
    }

    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}

