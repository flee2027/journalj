package lee.journalj.Data.Util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseMigrator {
    public static void migrate() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS news ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "title TEXT NOT NULL, "
                + "content TEXT NOT NULL, "
                + "publication_date TEXT NOT NULL);";

        try (Connection conn = DatabaseHandler.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create table", e);
        }
    }
}