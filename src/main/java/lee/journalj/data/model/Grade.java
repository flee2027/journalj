package lee.journalj.data.model;

import java.time.LocalDate;

public class Grade {
    private Long id;
    private Long studentId;
    private String subject;
    private LocalDate date;
    private int value;
    private String comment;

    public Grade() {
    }

    public Grade(Long studentId, String subject, LocalDate date, int value, String comment) {
        this.studentId = studentId;
        this.subject = subject;
        this.date = date;
        this.value = value;
        this.comment = comment;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
} 