package lee.journalj.data.util;

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

        // Создание таблицы уроков
        String createLessonsTableSQL = "CREATE TABLE IF NOT EXISTS lesson ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "subject TEXT NOT NULL, "
                + "day_of_week TEXT NOT NULL, "
                + "start_time TEXT NOT NULL, "
                + "end_time TEXT NOT NULL, "
                + "classroom TEXT, "
                + "homework_id INTEGER REFERENCES homework(id));";

// Создание таблицы домашних заданий
        String createHomeworksTableSQL = "CREATE TABLE IF NOT EXISTS homework ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "title TEXT NOT NULL, "
                + "content TEXT NOT NULL, "
                + "due_date TEXT NOT NULL);";

        try (Connection conn = DatabaseHandler.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            stmt.execute(createLessonsTableSQL);
            stmt.execute(createHomeworksTableSQL);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create table", e);
        }
    }
}