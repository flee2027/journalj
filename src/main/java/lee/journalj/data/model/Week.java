package lee.journalj.data.model;

import java.time.LocalDate;
import java.util.List;

public class Week {
    private LocalDate startDate;
    private List<Lesson> lessons;

    public Week(LocalDate startDate, List<Lesson> lessons) {
        this.startDate = startDate;
        this.lessons = lessons;
    }

    // Геттеры
}
