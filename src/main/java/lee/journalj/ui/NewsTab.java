package lee.journalj.ui;

import javafx.scene.Node;
import lee.journalj.data.model.News;
import lee.journalj.data.util.DatabaseMigrator;
import lee.journalj.service.NewsService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import lee.journalj.service.ScheduleService;

import java.util.List;

public class NewsTab {
    private final VBox content;
    private final ListView<News> newsListView;
    private final WebView newsContentWebView;
    private final NewsService newsService = new NewsService();
    private final ObservableList<News> newsList = FXCollections.observableArrayList();
    private News selectedNews;

    public NewsTab(ScheduleService scheduleService) {
        // Инициализация БД
        DatabaseMigrator.migrate();
        loadNews();

        // Создание компонентов
        newsListView = new ListView<>(newsList);
        newsListView.setCellFactory(param -> new NewsListCell());
        newsListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    selectedNews = newValue;
                    showNewsContent(newValue);
                });

        newsContentWebView = new WebView();
        newsContentWebView.setPrefHeight(300);

        // Кнопки управления
        Button addButton = new Button("Добавить новость");
        addButton.setOnAction(e -> showNewsDialog(null));

        Button editButton = new Button("Редактировать");
        editButton.setDisable(true);
        editButton.setOnAction(e -> showNewsDialog(selectedNews));

        Button deleteButton = new Button("Удалить");
        deleteButton.setDisable(true);
        deleteButton.setOnAction(e -> deleteSelectedNews());

        // Обновляем состояние кнопок при выборе новости
        newsListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    boolean isSelected = newVal != null;
                    editButton.setDisable(!isSelected);
                    deleteButton.setDisable(!isSelected);
                });

        // Панель кнопок
        HBox buttonBox = new HBox(10, addButton, editButton, deleteButton);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        // Область просмотра + кнопки
        VBox viewBox = new VBox(10, newsContentWebView, buttonBox);
        viewBox.setPadding(new Insets(0, 0, 0, 10));

        // Основной layout
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(newsListView, viewBox);
        splitPane.setDividerPositions(0.3);

        VBox container = new VBox(10, splitPane);
        container.setPadding(new Insets(10));
        container.setFillWidth(true);

        content = new VBox(container);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
    }

    private void loadNews() {
        new Thread(() -> {
            List<News> news = newsService.getAllNews();
            Platform.runLater(() -> newsList.setAll(news));
        }).start();
    }



    private void showNewsContent(News news) {
        if (news != null) {
            String htmlContent = "<html><body style='font-family: Arial, sans-serif; padding: 15px;'>" +
                    "<h1 style='color: #2c3e50;'>" + news.getTitle() + "</h1>" +
                    "<p style='color: #7f8c8d; font-size: 0.9em;'>" +
                    news.getPublicationDate() + "</p>" +
                    "<hr style='border: 1px solid #ecf0f1;'>" +
                    "<div style='line-height: 1.6;'>" + news.getContent() + "</div>" +
                    "</body></html>";
            newsContentWebView.getEngine().loadContent(htmlContent);
        } else {
            newsContentWebView.getEngine().loadContent("");
        }
    }

    private void showNewsDialog(News news) {
        boolean isNew = news == null;

        Dialog<News> dialog = new Dialog<>();
        dialog.setTitle(isNew ? "Добавить новость" : "Редактировать новость");

        ButtonType saveButtonType = new ButtonType(isNew ? "Добавить" : "Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField titleField = new TextField();
        TextArea contentArea = new TextArea();
        contentArea.setPrefRowCount(10);

        if (!isNew) {
            titleField.setText(news.getTitle());
            contentArea.setText(news.getContent());
        }

        grid.add(new Label("Заголовок:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Содержимое:"), 0, 1);
        grid.add(contentArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        titleField.textProperty().addListener((obs, oldVal, newVal) -> {
            saveButton.setDisable(newVal.trim().isEmpty());
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (isNew) {
                    News newNews = new News(titleField.getText(), contentArea.getText());
                    return newNews;
                } else {
                    news.setTitle(titleField.getText());
                    news.setContent(contentArea.getText());
                    return news;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (isNew) {
                saveNews(result);
            } else {
                updateNews(result);
            }
        });
    }


    private void saveNews(News news) {
        if (news.getTitle() == null || news.getTitle().trim().isEmpty()) {
            showError("Заголовок не может быть пустым.");
            return;
        }
        if (news.getContent() == null || news.getContent().trim().isEmpty()) {
            showError("Содержимое новости не может быть пустым.");
            return;
        }

        new Thread(() -> {
            newsService.saveNews(news);
            Platform.runLater(() -> {
                newsList.add(0, news);
                newsListView.getSelectionModel().select(0);
            });
        }).start();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateNews(News news) {
        new Thread(() -> {
            newsService.updateNews(news);
            Platform.runLater(() -> {
                int index = newsList.indexOf(news);
                if (index >= 0) {
                    newsList.set(index, news);
                    newsListView.getSelectionModel().select(index);
                }
            });
        }).start();
    }

    private void deleteSelectedNews() {
        if (selectedNews == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удаление новости");
        alert.setContentText("Вы уверены, что хотите удалить новость \"" + selectedNews.getTitle() + "\"?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    newsService.deleteNews(selectedNews.getId());
                    Platform.runLater(() -> {
                        newsList.remove(selectedNews);
                        selectedNews = null;
                        newsContentWebView.getEngine().loadContent("");
                    });
                }).start();
            }
        });
    }

    // Кастомная ячейка для отображения новости в списке
    private static class NewsListCell extends ListCell<News> {
        @Override
        protected void updateItem(News news, boolean empty) {
            super.updateItem(news, empty);
            if (empty || news == null) {
                setText(null);
            } else {
                setText(news.getTitle() + "\n" +
                        news.getPublicationDate().toLocalDate() + " " +
                        news.getPublicationDate().toLocalTime().toString().substring(0, 5));
            }
        }
    }

    public VBox getContent() {
        return content;
    }
}