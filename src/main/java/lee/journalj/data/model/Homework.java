package lee.journalj.data.model;

import java.time.LocalDate;

public class Homework {
    private int id;
    private String content;
    private LocalDate dueDate;

    public Homework() {
        // Конструктор без параметров для работы с базой данных
    }

    public Homework(int id, String content, LocalDate dueDate) {
        this.id = id;
        this.content = content != null ? content : ""; // Защита от null
        this.dueDate = dueDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}