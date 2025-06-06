package lee.journalj.ui;

import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import lee.journalj.data.model.Lesson;
import lee.journalj.data.model.Week;
import lee.journalj.service.ScheduleService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Представление недели с уроками.
 */
public class WeekView {
    private final VBox view;
    private final ScheduleService scheduleService;
    private final ScheduleTab scheduleTab;

    /**
     * Конструктор с внедрением зависимостей.
     * @param startDate начало недели
     * @param allLessons список всех уроков
     * @param scheduleService сервис расписания
     * @param scheduleTab ссылка на вкладку расписания (если нужно взаимодействие)
     */
    public WeekView(LocalDate startDate, List<Lesson> allLessons, ScheduleService scheduleService, ScheduleTab scheduleTab) {
        this.view = new VBox(10);
        this.scheduleService = scheduleService;
        this.scheduleTab = scheduleTab;

        Label weekLabel = new Label("Неделя: " + formatWeek(startDate));
        view.getChildren().add(weekLabel);

        // Группируем уроки по дням недели
        Map<LocalDate, VBox> dayViews = new HashMap<>();
        for (Lesson lesson : allLessons) {
            int dayOfWeekNumber = getDayOfWeekNumber(lesson.getDayOfWeek());
            LocalDate lessonDate = startDate.plusDays(dayOfWeekNumber - 1);
            LessonCard lessonCard = new LessonCard(lesson, scheduleService, scheduleTab);
            VBox dayBox = dayViews.computeIfAbsent(lessonDate, k -> new VBox(5));
            dayBox.getChildren().add(lessonCard);
        }
        dayViews.forEach((date, box) -> {
            Label dateLabel = new Label(date.format(DateTimeFormatter.ofPattern("EEEE, dd.MM")));
            view.getChildren().addAll(dateLabel, box);
        });
    }

    private String formatWeek(LocalDate start) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate end = start.plusDays(6);
        return start.format(formatter) + " – " + end.format(formatter);
    }

    /**
     * Преобразует строковое представление дня недели в числовой формат
     * @param dayOfWeek Строковое представление дня недели
     * @return Числовое представление дня недели (1 - понедельник, ..., 7 - воскресенье)
     */
    private int getDayOfWeekNumber(String dayOfWeek) {
        switch (dayOfWeek) {
            case "Понедельник": return 1;
            case "Вторник": return 2;
            case "Среда": return 3;
            case "Четверг": return 4;
            case "Пятница": return 5;
            case "Суббота": return 6;
            case "Воскресенье": return 7;
            default: throw new IllegalArgumentException("Неизвестный день недели: " + dayOfWeek);
        }
    }

    /**
     * Получить представление недели.
     */
    public VBox getView() {
        return view;
    }
}
