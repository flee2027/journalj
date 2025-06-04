package lee.journalj.UI;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ScheduleTab {
    private final VBox content;

    public ScheduleTab() {
        Label placeholder = new Label("Здесь будет расписание...");
        content = new VBox(placeholder);
    }

    public VBox getContent() {
        return content;
    }
}