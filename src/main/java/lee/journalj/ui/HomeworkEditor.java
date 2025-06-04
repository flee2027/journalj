package lee.journalj.ui;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.web.HTMLEditor;
import lee.journalj.data.model.Homework;

public class HomeworkEditor {
    public static void showEditDialog(Homework homework, Runnable onSave) {
        // Добавлена защита от null
        if (homework == null) {
            homework = new Homework(); // Создаем новое домашнее задание, если передан null
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Редактировать домашнее задание");
        HTMLEditor editor = new HTMLEditor();
        editor.setHtmlText(homework.getContent() != null ? homework.getContent() : "");

        dialog.getDialogPane().setContent(editor);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> btn == ButtonType.OK ? editor.getHtmlText() : null);

        Homework finalHomework = homework; // Добавляем final переменную
        dialog.showAndWait().ifPresent(result -> {
            if (result != null && !result.isEmpty()) {
                finalHomework.setContent(result); // Используем final переменную
                onSave.run();
            }
        });
    }
}