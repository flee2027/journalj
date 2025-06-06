package lee.journalj.data.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Модель домашнего задания для электронного журнала.
 */
public class Homework {
    private Long id;
    private String content;
    private LocalDate dueDate;
    private String title;
    private Long lessonId;

    /**
     * Конструктор без параметров для работы с базой данных.
     */
    public Homework() {
        this.content = "";
        this.title = "";
    }

    /**
     * Конструктор с параметрами.
     */
    public Homework(Long id, String content, LocalDate dueDate) {
        this.id = id;
        this.content = content != null ? content : "";
        this.dueDate = dueDate;
        this.title = "";
    }

    /**
     * Конструктор с параметрами и заголовком.
     */
    public Homework(Long id, String content, LocalDate dueDate, String title) {
        this.id = id;
        this.content = content != null ? content : "";
        this.dueDate = dueDate;
        this.title = title != null ? title : "";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content != null ? content : ""; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title != null ? title : ""; }

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Homework homework = (Homework) o;
        return id.equals(homework.id) &&
                Objects.equals(content, homework.content) &&
                Objects.equals(dueDate, homework.dueDate) &&
                Objects.equals(title, homework.title) &&
                Objects.equals(lessonId, homework.lessonId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, content, dueDate, title, lessonId);
    }
}