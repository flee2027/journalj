package lee.journalj.ui;

/**
 * Функциональный интерфейс для слушателя изменений данных в UI.
 */
@FunctionalInterface
public interface DataChangeListener {
    /**
     * Метод вызывается при изменении данных.
     */
    void onDataChanged();
}
