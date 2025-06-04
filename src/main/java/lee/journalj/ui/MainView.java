package lee.journalj.UI;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

public class MainView {
    private final TabPane tabPane;

    public MainView() {
        tabPane = new TabPane();

        // Создаем вкладки
        Tab newsTab = new Tab("Новости", new NewsTab().getContent());
        Tab scheduleTab = new Tab("Расписание", new ScheduleTab().getContent());
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