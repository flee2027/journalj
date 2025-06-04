package lee.journalj.UI;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class GradesTab {
    private final VBox content;

    public GradesTab() {
        Label placeholder = new Label("Здесь будут оценки...");
        content = new VBox(placeholder);
    }

    public VBox getContent() {
        return content;
    }
}