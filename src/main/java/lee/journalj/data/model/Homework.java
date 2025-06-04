package lee.journalj.data.model;

public class Homework {
    private int id;
    private String content;

    // Конструкторы, геттеры и сеттеры

    public Homework(int id, String content) {
        this.id = id;
        this.content = content;
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
}