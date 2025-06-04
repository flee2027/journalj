package lee.journalj;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lee.journalj.data.util.DatabaseHandler;
import lee.journalj.data.util.DatabaseMigrator;
import lee.journalj.ui.MainView;

public class Main extends Application {
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite driver not found: " + e.getMessage());
            System.exit(1);
        }

        DatabaseHandler.DatabaseConfig config = new DatabaseHandler.DatabaseConfig(
                DatabaseHandler.DatabaseType.SQLITE,
                "jdbc:sqlite:journal.db",
                "",
                ""
        );

        DatabaseHandler.init(config); // <-- Сначала инициализируем
        DatabaseMigrator.migrate(); // <-- потом миграция
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        MainView mainView = new MainView();
        Scene scene = new Scene(mainView.getView(), 600, 400);

        primaryStage.setTitle("JournalJ");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}