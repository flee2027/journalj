package lee.journalj.ui;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import lee.journalj.data.model.Lesson;
import lee.journalj.service.ScheduleService;

public class LessonCard extends VBox {
    public LessonCard(Lesson lesson, ScheduleService scheduleService, ScheduleTab scheduleTab) {
        this.scheduleTab = scheduleTab;
        setSpacing(5);
        getStyleClass().add("lesson-card");

        Text subjectText = new Text("📚 " + lesson.getSubject());
        Button editButton = new Button("Редактировать");
        
        editButton.setOnAction(e -> showEditDialog(lesson, scheduleService));

        VBox contentBox = new VBox(5);
        contentBox.getChildren().addAll(subjectText, editButton);
        
        getChildren().add(contentBox);
    }

    private final ScheduleTab scheduleTab;

    private void showEditDialog(Lesson lesson, ScheduleService scheduleService) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Редактировать урок");
        dialog.setHeaderText("Измените данные урока");

        TextField subjectField = new TextField(lesson.getSubject());

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Предмет:"), subjectField);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
                    if (buttonType == ButtonType.OK) {
                        lesson.setSubject(subjectField.getText());
                        scheduleService.updateLesson(lesson); // 仅传递 lesson 对象
                    }
                    return null;
                });

        dialog.showAndWait();
    }
}
