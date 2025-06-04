package lee.journalj.ui;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import lee.journalj.data.repository.implementation.LessonRepositoryImplementation;
import lee.journalj.data.repository.implementation.HomeworkRepositoryImplementation;
import lee.journalj.service.HomeworkService;
import lee.journalj.service.ScheduleService;

public class MainView {
    private final TabPane tabPane;

    public MainView(ScheduleService scheduleService) {
        tabPane = new TabPane();

        // Создаем сервисы для ScheduleTab
        HomeworkRepositoryImplementation homeworkRepo = new HomeworkRepositoryImplementation();
        HomeworkService homeworkService = new HomeworkService(homeworkRepo);
        LessonRepositoryImplementation lessonRepo = new LessonRepositoryImplementation();

        // Создаем вкладки
        Tab newsTab = new Tab("Новости", new NewsTab(scheduleService).getContent());
        Tab scheduleTab = new Tab("Расписание",
                new ScheduleTab(scheduleService, homeworkService, lessonRepo).getContent()); // Передаем правильные зависимости
        Tab gradesTab = new Tab("Оценки", new GradesTab().getContent());

        // Делаем вкладки не закрываемыми
        newsTab.setClosable(false);
        scheduleTab.setClosable(false);
        gradesTab.setClosable(false);

        // Добавляем вкладки в TabPane
        tabPane.getTabs().addAll(newsTab, scheduleTab, gradesTab);
    }

    public VBox getView() {
        return new VBox(tabPane);
    }
}