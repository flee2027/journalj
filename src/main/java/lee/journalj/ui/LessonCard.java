package lee.journalj.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import lee.journalj.data.model.Homework;
import lee.journalj.data.model.Lesson;
import lee.journalj.ui.HomeworkEditor;

import java.time.format.DateTimeFormatter;

public class LessonCard {
    private final VBox card;

    public LessonCard(Lesson lesson) {
        Label subjectLabel = new Label("Предмет: " + lesson.getSubject());
        Label timeLabel = new Label(
                lesson.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                        + " – " +
                        lesson.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))
        );
        Label classroomLabel = new Label("Аудитория: " + lesson.getClassroom());

        WebView homeworkPreview = new WebView();
        
        // Исправление 1: Улучшена проверка null для избежания NullPointerException
        if (lesson.getHomeworkId() != 0 && lesson.getHomework() != null) {
            Homework homework = lesson.getHomework();
            if (homework != null && homework.getContent() != null && !homework.getContent().isEmpty()) {
                homeworkPreview.getEngine().loadContent(homework.getContent());
            } else {
                homeworkPreview.getEngine().loadContent("<p>Нет содержимого домашнего задания</p>");
            }
        } else {
            homeworkPreview.getEngine().loadContent("<p>Нет домашнего задания</p>");
        }

        Button editHomeworkBtn = new Button("Редактировать домашнее задание");
        editHomeworkBtn.setOnAction(e -> {
            Homework homework = lesson.getHomework();
            if (homework == null) {
                homework = new Homework(); // Создаем новое домашнее задание, если его нет
                lesson.setHomework(homework);
            }
            Homework finalHomework = homework;
            HomeworkEditor.showEditDialog(homework, () -> {
                String content = finalHomework.getContent();
                if (content != null && !content.isEmpty()) {
                    homeworkPreview.getEngine().loadContent(content);
                } else {
                    homeworkPreview.getEngine().loadContent("<p>Нет обновленного содержимого</p>");
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