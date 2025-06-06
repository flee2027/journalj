package lee.journalj.data.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Модель урока для электронного журнала.
 */
@SuppressWarnings("opens")
public class Lesson {
    private Long id;
    private String subject;
    private String dayOfWeek;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;
    private Long homeworkId;
    // private Homework homework; // Удалено для однозначности, если не используется явно

    /**
     * Конструктор по умолчанию.
     */
    public Lesson() {
        this.id = -1L;
        this.subject = "";
        this.dayOfWeek = "";
        this.room = "";
    }

    /**
     * Конструктор с параметрами.
     */
    public Lesson(Long id, String subject, LocalTime startTime, LocalTime endTime, String room, Long homeworkId) {
        this.id = id;
        this.subject = subject;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.homeworkId = homeworkId;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public String getSubject() { return subject; }
    public String getDayOfWeek() { return dayOfWeek; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getRoom() { return room; }
    public Long getHomeworkId() { return homeworkId; }

    public void setId(Long id) { this.id = id; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setDayOfWeek(String dayOfWeek) {
        if (dayOfWeek == null || dayOfWeek.trim().isEmpty()) {
            this.dayOfWeek = null;
            return;
        }
        this.dayOfWeek = normalizeDayOfWeek(dayOfWeek);
    }
    public void setDate(LocalDate date) { this.date = date; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public void setRoom(String room) { this.room = room; }
    public void setHomeworkId(Long homeworkId) { this.homeworkId = homeworkId; }
    
    @Override
    public String toString() {
        return subject + " (" + dayOfWeek + ") " + startTime + " - " + endTime;
    }

    /**
     * Нормализует день недели к стандартному формату.
     */
    public static String normalizeDayOfWeek(String dayOfWeek) {
        if (dayOfWeek == null) return null;
        String normalized = dayOfWeek.trim().toLowerCase();
        switch (normalized) {
            case "понедельник":
            case "пн":
            case "monday":
                return "Понедельник";
            case "вторник":
            case "вт":
            case "tuesday":
                return "Вторник";
            case "среда":
            case "ср":
            case "wednesday":
                return "Среда";
            case "четверг":
            case "чт":
            case "thursday":
                return "Четверг";
            case "пятница":
            case "пт":
            case "friday":
                return "Пятница";
            case "суббота":
            case "сб":
            case "saturday":
                return "Суббота";
            case "воскресенье":
            case "вс":
            case "sunday":
                return "Воскресенье";
            default:
                return dayOfWeek;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lesson lesson = (Lesson) o;
        return id.equals(lesson.id) &&
                Objects.equals(subject, lesson.subject) &&
                Objects.equals(dayOfWeek, lesson.dayOfWeek) &&
                Objects.equals(date, lesson.date) &&
                Objects.equals(startTime, lesson.startTime) &&
                Objects.equals(endTime, lesson.endTime) &&
                Objects.equals(room, lesson.room) &&
                Objects.equals(homeworkId, lesson.homeworkId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, subject, dayOfWeek, date, startTime, endTime, room, homeworkId);
    }
}