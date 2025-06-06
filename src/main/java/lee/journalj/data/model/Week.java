package lee.journalj.data.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Модель недели для электронного журнала.
 */
public class Week {
    private LocalDate startDate;
    private List<Lesson> lessons;

    public Week(LocalDate startDate, List<Lesson> lessons) {
        this.startDate = startDate;
        this.lessons = lessons;
    }

    public LocalDate getStartDate() { return startDate; }
    public List<Lesson> getLessons() { return lessons; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Week week = (Week) o;
        return Objects.equals(startDate, week.startDate) &&
                Objects.equals(lessons, week.lessons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, lessons);
    }
}
