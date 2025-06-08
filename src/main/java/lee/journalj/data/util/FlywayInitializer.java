package lee.journalj.data.util;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;

public class FlywayInitializer {
    public static void migrate() {
        try {
            Flyway flyway = Flyway.configure()
                .dataSource(DatabaseHandler.getInstance().getConfig().getUrl(), 
                           DatabaseHandler.getInstance().getConfig().getUser(),
                           DatabaseHandler.getInstance().getConfig().getPassword())
                .locations("filesystem:src/main/resources/db/migration")
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .load();
            
            flyway.migrate();
        } catch (Exception e) {
            System.err.println("Failed to run Flyway migrations: " + e.getMessage());
            throw new RuntimeException("Failed to run Flyway migrations", e);
        }
    }
} 