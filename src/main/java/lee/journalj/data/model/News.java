package lee.journalj.data.model;

import java.time.LocalDateTime;

public class News {
    private int id;
    private String title;
    private String content;
    private LocalDateTime publicationDate;

    public News() {}

    public News(String title, String content) {
        this.title = title;
        this.content = content;
        this.publicationDate = LocalDateTime.now();
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDateTime publicationDate) {
        this.publicationDate = publicationDate;
    }
}