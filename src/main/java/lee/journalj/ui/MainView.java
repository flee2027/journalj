package lee.journalj.ui;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lee.journalj.data.repository.LessonRepository;
import lee.journalj.data.repository.HomeworkRepository;
import lee.journalj.data.repository.NewsRepository;
import lee.journalj.service.HomeworkService;
import lee.journalj.service.ScheduleService;
import lee.journalj.service.NewsService;
import lee.journalj.service.GradeService;
import lee.journalj.service.StudentService;

/**
 * Основное представление приложения с вкладками.
 */
public class MainView {
    private static final String TAB_SCHEDULE = "📅 Расписание";
    private static final String TAB_GRADES = "📊 Оценки";
    private static final String TAB_NEWS = "📰 Новости";

    private final TabPane tabPane;
    private final LessonRepository lessonRepo;
    private final HomeworkRepository homeworkRepo;
    private final NewsRepository newsRepo;
    private final ScheduleService scheduleService;
    private final HomeworkService homeworkService;
    private final NewsService newsService;
    private final GradeService gradeService;
    private final StudentService studentService;

    /**
     * Конструктор основного представления.
     */
    public MainView(LessonRepository lessonRepo, HomeworkRepository homeworkRepo, NewsRepository newsRepo,
                    ScheduleService scheduleService, HomeworkService homeworkService, NewsService newsService,
                    GradeService gradeService, StudentService studentService) {
        this.lessonRepo = lessonRepo;
        this.homeworkRepo = homeworkRepo;
        this.newsRepo = newsRepo;
        this.scheduleService = scheduleService;
        this.homeworkService = homeworkService;
        this.newsService = newsService;
        this.gradeService = gradeService;
        this.studentService = studentService;
        this.tabPane = new TabPane();

        // Настройка стиля вкладок
        tabPane.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Добавляем обработчик выбора вкладки
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && newTab.getContent() == null) {
                Node content = createContentForTab(newTab.getText());
                newTab.setContent(content);
            }
        });

        // Создаем вкладки
        Tab newsTab = new Tab(TAB_NEWS);
        Tab scheduleTab = new Tab(TAB_SCHEDULE);
        Tab gradesTab = new Tab(TAB_GRADES);

        // Настраиваем стиль вкладок
        String tabStyle = "-fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-color: transparent;";
        newsTab.setStyle(tabStyle);
        scheduleTab.setStyle(tabStyle);
        gradesTab.setStyle(tabStyle);

        tabPane.getTabs().addAll(newsTab, scheduleTab, gradesTab);
    }

    /**
     * Создать содержимое для выбранной вкладки.
     */
    private Node createContentForTab(String tabName) {
        VBox content = new VBox();
        content.setStyle("-fx-padding: 20px; -fx-background-color: #f8f9fa;");

        switch (tabName) {
            case TAB_SCHEDULE:
                content.getChildren().add(new ScheduleTab(scheduleService, homeworkService).getContent());
                break;
            case TAB_GRADES:
                content.getChildren().add(new GradesTab(gradeService, studentService, scheduleService).getContent());
                break;
            case TAB_NEWS:
                content.getChildren().add(new NewsTab(newsService).getContent());
                break;
            default:
                throw new IllegalArgumentException("Unknown tab: " + tabName);
        }

        return content;
    }

    /**
     * Получить главное представление.
     */
    public TabPane getView() {
        return tabPane;
    }

    public void show(Stage stage) {
        Scene scene = new Scene(tabPane);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        
        stage.setTitle("JournalJ");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }
}