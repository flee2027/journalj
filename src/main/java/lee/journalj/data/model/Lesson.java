package lee.journalj.data.model;

import java.time.LocalTime;

public class Lesson {
    private int id;
    private String content;
    private LocalTime startTime;
    private LocalTime endTime;
    private String subject;
    private String classroom;
    private int homeworkId;
    private Homework homework;
    private boolean completed;

    // Конструкторы, геттеры и сеттеры

    public Lesson(int id, String content) {
        this.id = id;
        this.content = content;
        this.homeworkId = homeworkId;
        this.homework = homework;
    }

    // Добавим конструктор без Homework для случаев, когда домашнего задания нет
    public Lesson(int id, String content, int homeworkId) {
        this.id = id;
        this.content = content;
        this.homeworkId = homeworkId;
        this.homework = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getContent() {
        return content;
    }
    public String getSubject() {
        return subject;
    }
    public String getClassroom() { return classroom; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setContent(String content) {
        this.content = content;
    }

    public int getHomeworkId() {
        return homeworkId;
    }

    public void setHomeworkId(int homeworkId) {
        this.homeworkId = homeworkId;
    }

    public Homework getHomework() {
        return homework;
    }

    public void setHomework(Homework homework) {
        this.homework = homework;
    }
}