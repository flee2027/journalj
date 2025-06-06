package lee.journalj;

import javafx.application.Application;
import javafx.stage.Stage;
import lee.journalj.data.repository.implementation.*;
import lee.journalj.service.*;
import lee.journalj.ui.MainView;
import lee.journalj.data.util.DatabaseHandler;
import lee.journalj.data.util.DatabaseHandler.DatabaseConfig;
import lee.journalj.data.util.DatabaseHandler.DatabaseType;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Main extends Application {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private GradeService gradeService;
    private StudentService studentService;
    private NewsService newsService;
    private HomeworkService homeworkService;
    private ScheduleService scheduleService;

    @Override
    public void start(Stage primaryStage) {
        try {
            initializeDatabase();
            initializeServices();
            MainView mainView = new MainView(
                new LessonRepositoryImplementation(),
                new HomeworkRepositoryImplementation(),
                new NewsRepositoryImplementation(),
                scheduleService,
                homeworkService,
                newsService,
                gradeService,
                studentService
            );
            mainView.show(primaryStage);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to start application", e);
            System.exit(1);
        }
    }

    private void initializeDatabase() throws Exception {
        try {
            // Initialize database configuration
            String dbPath = System.getProperty("user.dir") + "/journal.db";
            DatabaseConfig config = new DatabaseConfig(
                DatabaseType.SQLITE,
                "jdbc:sqlite:" + dbPath,
                "",
                ""
            );
            DatabaseHandler.init(config);

            // Initialize Flyway for database migrations
            DatabaseHandler dbHandler = DatabaseHandler.getInstance();
            FluentConfiguration flywayConfig = Flyway.configure()
                .dataSource(dbHandler.getConnection().getMetaData().getURL(), "", "")
                .locations("filesystem:src/main/resources/db/migration")
                .baselineOnMigrate(true)
                .validateOnMigrate(true);
            Flyway flyway = flywayConfig.load();
            flyway.migrate();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize database", e);
            throw e;
        }
    }

    private void initializeServices() {
        try {
            // Initialize repositories
            GradeRepositoryImplementation gradeRepository = new GradeRepositoryImplementation();
            StudentRepositoryImplementation studentRepository = new StudentRepositoryImplementation();
            NewsRepositoryImplementation newsRepository = new NewsRepositoryImplementation();
            HomeworkRepositoryImplementation homeworkRepository = new HomeworkRepositoryImplementation();
            LessonRepositoryImplementation lessonRepository = new LessonRepositoryImplementation();

            // Initialize services
            gradeService = new GradeService(gradeRepository);
            studentService = new StudentService(studentRepository);
            newsService = new NewsService(newsRepository);
            homeworkService = new HomeworkService(homeworkRepository);
            scheduleService = new ScheduleService(lessonRepository, homeworkRepository);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize services", e);
            throw new RuntimeException("Failed to initialize services", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}