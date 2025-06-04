package lee.journalj.ui;

import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import lee.journalj.data.model.Lesson;
import lee.journalj.data.model.Week;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WeekView {
    private final VBox view;

    public WeekView(LocalDate startDate, List<Lesson> allLessons) {
        this.view = new VBox(10);

        Label weekLabel = new Label("Неделя: " + formatWeek(startDate));
        view.getChildren().add(weekLabel);

        // Здесь можно разбить уроки по дням и отобразить их
        for (Lesson lesson : allLessons) {
            view.getChildren().add(new LessonCard(lesson).getView());
        }
    }

    private String formatWeek(LocalDate start) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate end = start.plusDays(6);
        return start.format(formatter) + " – " + end.format(formatter);
    }

    public VBox getView() {
        return view;
    }
}
