package lee.journalj.ui;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.geometry.*;
import lee.journalj.data.model.Lesson;
import lee.journalj.data.service.ScheduleService;
import lee.journalj.ui.HomeworkEditor;
import lee.journalj.ui.WeekView;

import java.time.LocalDate;
import java.util.List;

public class ScheduleTab {
    private final VBox content;
    private final ScheduleService scheduleService;

    public ScheduleTab(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
        this.content = new VBox(10);
        loadWeekViews();
    }

    private void loadWeekViews() {
        List<Lesson> lessons = scheduleService.getAllLessons();

        // Пример: отображение текущей недели
        WeekView currentWeek = new WeekView(LocalDate.now(), lessons);
        content.getChildren().add(currentWeek.getView());
    }

    public VBox getContent() {
        return content;
    }
}
