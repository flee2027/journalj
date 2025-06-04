package lee.journalj.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import lee.journalj.data.model.Homework;
import lee.journalj.data.model.Lesson;
import lee.journalj.ui.HomeworkEditor;

public class LessonCard {
    private final VBox card;

    public LessonCard(Lesson lesson) {
        Label subjectLabel = new Label("Предмет: " + lesson.getSubject());
        Label timeLabel = new Label(
                lesson.getStartTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                        + " – " +
                        lesson.getEndTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
        );
        Label classroomLabel = new Label("Аудитория: " + lesson.getClassroom());

        WebView homeworkPreview = new WebView();

        if (lesson.getHomeworkId() != 0 && lesson.getHomework() != null) {
            // Проверка на null для Homework объекта
            if (lesson.getHomework().getContent() != null) {
                homeworkPreview.getEngine().loadContent(lesson.getHomework().getContent());
            } else {
                homeworkPreview.getEngine().loadContent("<p>Нет содержимого домашнего задания</p>");
            }
        } else {
            homeworkPreview.getEngine().loadContent("<p>Нет домашнего задания</p>");
        }

        Button editHomeworkBtn = new Button("Редактировать домашнее задание");
        editHomeworkBtn.setOnAction(e -> {
            HomeworkEditor.showEditDialog(lesson.getHomework(), (Homework updatedHomework) -> {
                if (updatedHomework != null) {
                    String content = updatedHomework.getContent();
                    if (content != null) {
                        homeworkPreview.getEngine().loadContent(content);
                    } else {
                        homeworkPreview.getEngine().loadContent("<p>Нет содержимого домашнего задания</p>");
                    }
                } else {
                    homeworkPreview.getEngine().loadContent("<p>Нет обновленного домашнего задания</p>");
                }
            });
        });


        card = new VBox(
                new HBox(subjectLabel),
                new HBox(timeLabel),
                new HBox(classroomLabel),
                homeworkPreview,
                editHomeworkBtn
        );
        card.setSpacing(5);
        card.setStyle("-fx-border-color: #ccc; -fx-padding: 10px;");
    }

    public VBox getView() {
        return card;
    }
}