package lee.journalj.data.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Класс для миграции и создания таблиц базы данных.
 */
public class DatabaseMigrator {
    /**
     * Выполняет миграции (создание таблиц, если их нет).
     */
    public static void migrate() {
        String[] migrationSQL = {
                // Таблица новостей
                "CREATE TABLE IF NOT EXISTS news (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "title TEXT NOT NULL, " +
                        "content TEXT NOT NULL, " +
                        "publication_date TEXT NOT NULL, " +
                        "author TEXT NOT NULL);",
                // Таблица уроков
                "CREATE TABLE IF NOT EXISTS lesson (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "subject TEXT NOT NULL, " +
                        "room TEXT NOT NULL, " +
                        "day_of_week TEXT NOT NULL, " +
                        "start_time TEXT NOT NULL, " +
                        "end_time TEXT NOT NULL, " +
                        "homework_id INTEGER REFERENCES homework(id));",
                // Таблица домашних заданий
                "CREATE TABLE IF NOT EXISTS homework (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "title TEXT NOT NULL, " +
                        "content TEXT NOT NULL, " +
                        "due_date TEXT NOT NULL, " +
                        "lesson_id INTEGER REFERENCES lesson(id));"
        };

        try (Connection conn = DatabaseHandler.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sql : migrationSQL) {
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseMigrator] Failed to create tables: " + e.getMessage());
            throw new RuntimeException("Failed to create tables", e);
        }
    }
}