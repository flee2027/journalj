package lee.journalj.data.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Lesson {
    private int id;
    private String content;
    private LocalTime startTime;
    private LocalDate date; // Новое поле для хранения даты урока
    private String name;
    private LocalTime endTime;
    private String subject;
    private String classroom;
    private Integer homeworkId; // Изменяем тип с int на Integer
    private Homework homework;
    private boolean completed;
    private String dayOfWeek; // День недели

    // Конструкторы, геттеры и сеттеры

    public Lesson(int id, String content) {
        this.id = id;
        this.content = content;
        this.homeworkId = 0; // Исправлено: инициализация homeworkId
        this.homework = null; // Исправлено: инициализация homework
    }

    // Добавим конструктор без Homework для случаев, когда домашнего задания нет
    public Lesson(int id, String content, int homeworkId) {
        this.id = id;
        this.content = content;
        this.homeworkId = homeworkId;
        this.homework = null;
    }

    public Lesson(int id, String subject, LocalTime startTime, LocalTime endTime, String classroom, Integer homeworkId) {
        this.id = id;
        this.subject = subject;
        this.startTime = startTime;
        this.endTime = endTime;
        this.classroom = classroom;
        this.homeworkId = homeworkId;
    }

    public Lesson(int id, String subject, LocalTime startTime, LocalTime endTime, Integer homeworkId) {
        this.id = id;
        this.subject = subject != null ? subject : ""; // Защита от null
        this.startTime = startTime;
        this.endTime = endTime;
        this.homeworkId = homeworkId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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

    public Integer getHomeworkId() {
        return homeworkId;
    }
    public String getName() { return name; }

    public void setHomeworkId(int homeworkId) {
        this.homeworkId = homeworkId;
    }

    public Homework getHomework() {
        return homework;
    }

    public void setHomework(Homework homework) {
        this.homework = homework;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}