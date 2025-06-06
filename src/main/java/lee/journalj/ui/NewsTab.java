package lee.journalj.ui;

import javafx.scene.Node;
import lee.journalj.data.model.News;
import lee.journalj.data.repository.implementation.NewsRepositoryImplementation;
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
import javafx.scene.layout.Pane;
import javafx.geometry.Pos;
import javafx.scene.web.HTMLEditor;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Вкладка новостей с отображением, добавлением и редактированием новостей.
 */
public class NewsTab implements TabContent {
    private final VBox content;
    private final ListView<News> newsListView;
    private final WebView newsWebView;
    private final NewsService newsService;
    private final ObservableList<News> newsList;
    private News selectedNews;

    /**
     * Конструктор с внедрением NewsService.
     */
    public NewsTab(NewsService newsService) {
        this.newsService = newsService;
        this.content = new VBox();
        this.newsList = FXCollections.observableArrayList();
        this.newsListView = new ListView<>(newsList);
        this.newsWebView = new WebView();
        
        initializeContent();
    }

    private void initializeContent() {
        content.setStyle("-fx-padding: 20px; -fx-background-color: #f8f9fa;");
        content.setSpacing(20);

        // Создаем список новостей
        newsListView.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-color: #e9ecef; -fx-border-width: 1px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        newsListView.setCellFactory(lv -> new ListCell<News>() {
            @Override
            protected void updateItem(News news, boolean empty) {
                super.updateItem(news, empty);
                if (empty || news == null) {
                    setText(null);
                    setStyle("-fx-background-color: white;");
                } else {
                    setText(news.getTitle());
                    if (news.equals(selectedNews)) {
                        setStyle("-fx-background-color: #e3f2fd; -fx-padding: 10px; -fx-border-color: #2196F3; -fx-border-width: 0 0 1px 0;");
                    } else {
                        setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-border-color: #e9ecef; -fx-border-width: 0 0 1px 0;");
                    }
                }
            }
        });
        newsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedNews = newVal;
                showNewsContent(newVal);
                newsListView.refresh();
            }
        });

        // Создаем область для отображения содержимого новости
        newsWebView.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-color: #e9ecef; -fx-border-width: 1px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        newsWebView.setPrefHeight(400);
        newsWebView.getEngine().setUserStyleSheetLocation("data:text/css," +
            "body { font-family: 'Segoe UI', Arial, sans-serif; padding: 20px; }" +
            "h1 { color: #2c3e50; margin-bottom: 10px; }" +
            "p { color: #34495e; line-height: 1.6; }" +
            "div { pointer-events: none; }" +
            "* { pointer-events: none; }");

        // Отключаем возможность редактирования в WebView
        newsWebView.setDisable(true);

        // Создаем кнопки управления
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button addButton = new Button("+ Добавить новость");
        addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 4px; -fx-padding: 8px 16px; -fx-font-weight: bold;");
        addButton.setOnAction(e -> showAddNewsDialog());

        Button editButton = new Button("✏️ Редактировать");
        editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 4px; -fx-padding: 8px 16px; -fx-font-weight: bold;");
        editButton.setOnAction(e -> {
            News selectedNews = newsListView.getSelectionModel().getSelectedItem();
            if (selectedNews != null) {
                showEditNewsDialog(selectedNews);
            }
        });

        Button deleteButton = new Button("🗑️ Удалить");
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 4px; -fx-padding: 8px 16px; -fx-font-weight: bold;");
        deleteButton.setOnAction(e -> {
            News selectedNews = newsListView.getSelectionModel().getSelectedItem();
            if (selectedNews != null) {
                deleteNews(selectedNews);
            }
        });

        buttonBox.getChildren().addAll(addButton, editButton, deleteButton);

        // Размещаем элементы
        HBox mainContent = new HBox(20);
        mainContent.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-padding: 20px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        
        VBox listBox = new VBox(10);
        listBox.getChildren().add(newsListView);
        listBox.setPrefWidth(300);
        
        VBox contentBox = new VBox(10);
        contentBox.getChildren().addAll(newsWebView, buttonBox);
        VBox.setVgrow(newsWebView, Priority.ALWAYS);
        
        mainContent.getChildren().addAll(listBox, contentBox);
        HBox.setHgrow(contentBox, Priority.ALWAYS);
        
        content.getChildren().add(mainContent);
        
        // Загружаем новости
        updateNewsList();
    }

    private void showNewsContent(News news) {
        String html = String.format("""
            <div style="font-family: 'Segoe UI', Arial, sans-serif;">
                <h1 style="color: #2c3e50; margin-bottom: 10px;">%s</h1>
                <p style="color: #7f8c8d; font-size: 0.9em; margin-bottom: 20px;">%s</p>
                <div style="color: #34495e; line-height: 1.6;">%s</div>
            </div>
            """, 
            news.getTitle(),
            news.getPublicationDate().format(DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm")),
            news.getContent()
        );
        newsWebView.getEngine().loadContent(html);
    }

    private void showAddNewsDialog() {
        Dialog<News> dialog = new Dialog<>();
        dialog.setTitle("Добавить новость");
        dialog.setHeaderText("Введите данные новости");

        // Создаем поля ввода
        TextField titleField = new TextField();
        titleField.setPromptText("Заголовок");
        titleField.setStyle("-fx-padding: 8px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

        HTMLEditor contentEditor = new HTMLEditor();
        contentEditor.setHtmlText("<html><body></body></html>");
        contentEditor.setPrefHeight(300);

        // Создаем форму
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white;");

        grid.add(new Label("Заголовок:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Содержание:"), 0, 1);
        grid.add(contentEditor, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Добавляем кнопки
        ButtonType addButtonType = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Обработка результата
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                News news = new News();
                news.setTitle(titleField.getText());
                news.setContent(contentEditor.getHtmlText());
                news.setPublicationDate(LocalDateTime.now());
                return news;
            }
            return null;
        });

        Optional<News> result = dialog.showAndWait();
        result.ifPresent(news -> {
            newsService.saveNews(news);
            updateNewsList();
        });
    }

    private void showEditNewsDialog(News news) {
        Dialog<News> dialog = new Dialog<>();
        dialog.setTitle("Редактировать новость");
        dialog.setHeaderText("Редактирование новости");

        TextField titleField = new TextField(news.getTitle());
        titleField.setPromptText("Заголовок новости");
        titleField.setStyle("-fx-background-color: white; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #e9ecef; -fx-border-width: 1px; -fx-padding: 8px;");

        HTMLEditor contentEditor = new HTMLEditor();
        contentEditor.setHtmlText(news.getContent());
        contentEditor.setPrefHeight(400);

        VBox content = new VBox(10, titleField, contentEditor);
        content.setStyle("-fx-padding: 20px;");
        dialog.getDialogPane().setContent(content);

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (titleField.getText().trim().isEmpty()) {
                    showErrorDialog("Пожалуйста, введите заголовок новости");
                    return null;
                }

                news.setTitle(titleField.getText().trim());
                news.setContent(contentEditor.getHtmlText());
                news.setPublicationDate(LocalDateTime.now());
                return news;
            }
            return null;
        });

        Optional<News> result = dialog.showAndWait();
        result.ifPresent(updatedNews -> {
            newsService.updateNews(updatedNews);
            refreshNewsList();
            if (selectedNews != null && selectedNews.getId() == updatedNews.getId()) {
                showNewsContent(updatedNews);
            }
        });
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void deleteNews(News news) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удалить новость?");
        alert.setContentText("Вы уверены, что хотите удалить новость \"" + news.getTitle() + "\"?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            newsService.deleteNews(news.getId());
            updateNewsList();
        }
    }

    private void updateNewsList() {
        newsList.clear();
        newsList.addAll(newsService.getAllNews());
    }

    private void refreshNewsList() {
        newsList.clear();
        List<News> allNews = newsService.getAllNews();
        newsList.addAll(allNews);
        newsListView.refresh();
    }

    @Override
    public Pane getContent() {
        return content;
    }
}