package lee.journalj.data.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Модель новости для электронного журнала.
 */
public class News {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime publicationDate;
    private String author;

    /**
     * Конструктор по умолчанию.
     */
    public News() {
        this.title = "";
        this.content = "";
        this.author = "";
        this.publicationDate = LocalDateTime.now();
    }

    /**
     * Конструктор с параметрами.
     */
    public News(Long id, String title, String content, LocalDateTime publicationDate, String author) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.publicationDate = publicationDate;
        this.author = author;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() {
        return content != null ? content : "<p>Нет содержимого</p>";
    }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDateTime publicationDate) { this.publicationDate = publicationDate; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        News news = (News) o;
        return id.equals(news.id) &&
                Objects.equals(title, news.title) &&
                Objects.equals(content, news.content) &&
                Objects.equals(publicationDate, news.publicationDate) &&
                Objects.equals(author, news.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content, publicationDate, author);
    }
}