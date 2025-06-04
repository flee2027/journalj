package lee.journalj.ui;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.web.HTMLEditor;
import lee.journalj.data.model.Homework;

public class HomeworkEditor {
    public static void showEditDialog(Homework homework, Runnable onSave) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Редактировать домашнее задание");
        dialog.setHeaderText(null);

        HTMLEditor editor = new HTMLEditor();
        editor.setHtmlText(homework.getContent());

        dialog.getDialogPane().setContent(editor);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> btn == ButtonType.OK ? editor.getHtmlText() : null);

        dialog.showAndWait().ifPresent(result -> {
            homework.setContent(result);
            onSave.run();
        });
    }
}
