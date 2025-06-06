package lee.journalj.ui;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.web.HTMLEditor;
import lee.journalj.data.model.Homework;

/**
 * Класс для отображения и редактирования домашнего задания в диалоге.
 */
public class HomeworkEditor {
    /**
     * Показывает диалог редактирования домашнего задания.
     * @param homework домашнее задание для редактирования (может быть null)
     * @param onSave действие, выполняемое после сохранения (может быть null)
     */
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
                if (onSave != null) {
                onSave.run();
                }
            }
        });
    }
}