package lee.journalj.data.util;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHandler {
    private static DatabaseConfig config;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite driver not found", e);
        }
    }

    public static void init(DatabaseConfig config) {
        DatabaseHandler.config = config;
        registerDriver();
    }

    private static void registerDriver() {
        try {
            switch (config.getType()) {
                case SQLITE -> Class.forName("org.sqlite.JDBC");
                case POSTGRESQL -> Class.forName("org.postgresql.Driver");
                default -> throw new IllegalArgumentException("Unsupported DB type");
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                config.getUrl(),
                config.getUser(),
                config.getPassword()
        );
    }

    public enum DatabaseType { SQLITE, POSTGRESQL }

    public static class DatabaseConfig {
        private final DatabaseType type;
        private final String url;
        private final String user;
        private final String password;

        public DatabaseConfig(DatabaseType type, String url, String user, String password) {
            this.type = type;
            this.url = url;
            this.user = user;
            this.password = password;
        }

        // Геттеры
        public DatabaseType getType() { return type; }
        public String getUrl() { return url; }
        public String getUser() { return user; }
        public String getPassword() { return password; }
    }
}
