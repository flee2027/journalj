package lee.journalj.data.util;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;

import java.io.File;

public class FlywayInitializer {
    public static void migrate() {
        try {
            String url = DatabaseHandler.getInstance().getConfig().getUrl();
            String migrationsLocation;

            // Проверяем, доступны ли миграции через filesystem (IDE)
            File localMigrationFile = new File("src/main/resources/db/migration/V1__create_tables.sql");
            if (localMigrationFile.exists()) {
                System.out.println("Running in IDE, using filesystem migrations");
                migrationsLocation = "filesystem:src/main/resources/db/migration";
            } else {
                System.out.println("Running in JAR, using classpath migrations");
                migrationsLocation = "classpath:db/migration";
            }

            Flyway flyway = Flyway.configure()
                    .dataSource(url, null, null)
                    .locations(migrationsLocation)
                    .baselineOnMigrate(true)
                    .validateOnMigrate(false) // Опционально: отключает строгую валидацию
                    .load();

            flyway.migrate();
        } catch (Exception e) {
            System.err.println("Failed to run Flyway migrations: " + e.getMessage());
            throw new RuntimeException("Failed to run Flyway migrations", e);
        }
    }
} 