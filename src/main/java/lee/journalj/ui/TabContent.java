package lee.journalj.ui;

import javafx.scene.layout.Pane;

/**
 * Интерфейс для стандартизации вкладок приложения.
 * Все вкладки должны реализовывать этот интерфейс.
 */
public interface TabContent {
    /**
     * Возвращает содержимое вкладки в виде Pane.
     * @return Pane с содержимым вкладки
     */
    Pane getContent();
} 