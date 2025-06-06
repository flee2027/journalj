package lee.journalj.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.scene.control.Label;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import javafx.scene.layout.Pane;
import lee.journalj.data.model.Homework;
import lee.journalj.data.model.Lesson;
import lee.journalj.data.repository.implementation.HomeworkRepositoryImplementation;
import lee.journalj.data.repository.implementation.ScheduleRepositoryImplementation;
import lee.journalj.service.HomeworkService;
import lee.journalj.service.ScheduleService;

import java.time.DayOfWeek;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;

/**
 * Вкладка расписания с отображением и управлением уроками.
 */
public class ScheduleTab implements TabContent {
    private final ScheduleService scheduleService;
    private final HomeworkService homeworkService;
    private VBox currentTableViewContainer;
    private TableView<Lesson> currentTableView;
    private VBox mainContainer;
    private DatePicker datePicker;
    private LocalDate currentDate;
    private Node currentView;
    private final VBox content;
    private final ComboBox<DayOfWeek> dayComboBox;
    private final ComboBox<String> subjectComboBox;
    private final ObservableList<Lesson> lessons;
    private final TableView<Lesson> lessonsTable;
    private HBox weekView;
    private LocalDate weekStart;
    
    /**
     * Конструктор с внедрением зависимостей.
     */
    public ScheduleTab() {
        // Инициализация сервисов
        ScheduleRepositoryImplementation scheduleRepo = new ScheduleRepositoryImplementation();
        HomeworkRepositoryImplementation homeworkRepo = new HomeworkRepositoryImplementation();
        scheduleService = new ScheduleService(scheduleRepo, homeworkRepo);
        homeworkService = new HomeworkService(homeworkRepo);

        // Инициализация списка уроков
        lessons = FXCollections.observableArrayList();

        // Создаем основной контейнер
        mainContainer = new VBox(10);
        mainContainer.setStyle("-fx-padding: 20px; -fx-background-color: #f8f9fa;");

        // Создаем контейнер для контента
        content = new VBox(10);
        content.setStyle("-fx-padding: 20px;");

        // Добавляем контент в основной контейнер
        mainContainer.getChildren().add(content);

        // Устанавливаем текущую дату
        currentDate = LocalDate.now();
        weekStart = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        dayComboBox = new ComboBox<>();
        subjectComboBox = new ComboBox<>();
        lessonsTable = new TableView<>();

        // Загружаем начальные данные
        refreshLessons();
        
        // Создаем начальное представление
        showWeek(currentDate);
    }
    
    /**
     * Добавить урок и обновить таблицу.
     */
    public void addLesson(Lesson lesson) {
        scheduleService.saveLesson(lesson);
        refreshLessons();
        updateWeekView();
        showWeek(currentDate);
    }
    
    /**
     * Инициализирует вкладку расписания
     */
    public void initialize() {
        // Устанавливаем обработчик изменения даты
        datePicker.setOnAction(e -> updateCurrentTableView());
        
        // Создаем начальный интерфейс с текущей неделей
        LocalDate currentDate = LocalDate.now();
        showWeek(currentDate);
        
        // Инициализируем текущую таблицу
        updateCurrentTableView();
    }
    
    private void reloadSchedule() {
        // Запоминаем текущую дату
        LocalDate currentDate = this.currentDate;
        
        // Приводим getContent() к VBox, так как это контейнер с детьми
        VBox contentContainer = (VBox) getContent();
        
        // Удаляем старое представление
        if (currentView != null) {
            contentContainer.getChildren().remove(currentView);
        }
        
        // Создаем новое представление с актуальными данными
        VBox newView = createDayView(currentDate);
        contentContainer.getChildren().add(newView);
        this.currentView = newView;
        
        // Обновляем данные через сервис
        refreshLessons();
    }

    private HBox createNavigationHeader() {
        HBox navBox = new HBox(10);
        navBox.setAlignment(Pos.CENTER);

        Button prevBtn = new Button("←");
        Button nextBtn = new Button("→");
        Label dateLabel = new Label(currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        dateLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        prevBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 4px; -fx-min-width: 40px;");
        nextBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 4px; -fx-min-width: 40px;");

        prevBtn.setOnAction(e -> {
            currentDate = currentDate.minusWeeks(1);
            weekStart = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            dateLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
            refreshLessons();
            showWeek(currentDate);
        });

        nextBtn.setOnAction(e -> {
            currentDate = currentDate.plusWeeks(1);
            weekStart = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            dateLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
            refreshLessons();
            showWeek(currentDate);
        });

        navBox.getChildren().addAll(prevBtn, dateLabel, nextBtn);
        return navBox;
    }

    private void editHomework(Lesson lesson) {
        final Homework homework;
        if (lesson.getHomeworkId() != null && lesson.getHomeworkId() > 0) {
            Optional<Homework> homeworkOpt = homeworkService.findById(lesson.getHomeworkId());
            homework = homeworkOpt.orElse(null);
        } else {
            homework = null;
        }
        
        if (homework == null) {
            // Если домашнего задания нет, показываем диалог создания
            showHomeworkDialog(lesson);
        } else {
            // Если домашнее задание уже существует, показываем диалог редактирования
            Dialog<Homework> dialog = new Dialog<>();
            dialog.setTitle("Редактирование домашнего задания");
            dialog.setHeaderText("Изменение домашнего задания");

            // Создаем форму
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField titleField = new TextField(homework.getTitle());
            titleField.setPromptText("Заголовок задания");

            TextArea descriptionArea = new TextArea(homework.getContent());
            descriptionArea.setPromptText("Описание задания");
            descriptionArea.setPrefRowCount(3);
            descriptionArea.setWrapText(true);

            DatePicker dueDatePicker = new DatePicker(homework.getDueDate());
            dueDatePicker.setPromptText("Срок сдачи");

            grid.add(new Label("Заголовок:"), 0, 0);
            grid.add(titleField, 1, 0);
            grid.add(new Label("Описание:"), 0, 1);
            grid.add(descriptionArea, 1, 1);
            grid.add(new Label("Срок сдачи:"), 0, 2);
            grid.add(dueDatePicker, 1, 2);

            dialog.getDialogPane().setContent(grid);

            // Добавляем кнопки
            ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Валидация
            Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
            saveButton.setDisable(true);

            titleField.textProperty().addListener((obs, oldVal, newVal) -> {
                saveButton.setDisable(newVal.trim().isEmpty());
            });

            // Обработка результата
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    homework.setTitle(titleField.getText());
                    homework.setContent(descriptionArea.getText());
                    homework.setDueDate(dueDatePicker.getValue());
                    return homework;
                }
                return null;
            });

            Optional<Homework> result = dialog.showAndWait();
            result.ifPresent(updatedHomework -> {
                try {
                    homeworkService.update(updatedHomework);
                    showSuccess("Успех", "Домашнее задание успешно обновлено");
                    refreshLessons();
                } catch (Exception e) {
                    showError("Ошибка", "Не удалось обновить домашнее задание: " + e.getMessage());
                }
            });
        }
    }

    private void showHomeworkDialog(Lesson lesson) {
        Dialog<Homework> dialog = new Dialog<>();
        dialog.setTitle("Новое домашнее задание");
        dialog.setHeaderText("Добавление домашнего задания");

        // Создаем форму
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Название задания");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Описание задания");
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);

        DatePicker dueDatePicker = new DatePicker(LocalDate.now().plusDays(1));

        grid.add(new Label("Название:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Описание:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Срок сдачи:"), 0, 2);
        grid.add(dueDatePicker, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Добавляем кнопки
        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Валидация
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        titleField.textProperty().addListener((obs, oldVal, newVal) -> {
            saveButton.setDisable(newVal == null || newVal.trim().isEmpty());
        });

        // Обработка результата
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Homework homework = new Homework();
                homework.setTitle(titleField.getText());
                homework.setContent(descriptionArea.getText());
                homework.setDueDate(dueDatePicker.getValue());
                homework.setLessonId(lesson.getId());
                return homework;
            }
            return null;
        });

        Optional<Homework> result = dialog.showAndWait();
        result.ifPresent(homework -> {
            try {
                homeworkService.save(homework);
                showSuccess("Успех", "Домашнее задание успешно добавлено");
                refreshLessons();
            } catch (Exception e) {
                showError("Ошибка", "Не удалось добавить домашнее задание: " + e.getMessage());
            }
        });
    }

    private void showAddLessonDialog(LocalDate date) {
        Dialog<Lesson> dialog = new Dialog<>();
        dialog.setTitle("Новый урок");
        dialog.setHeaderText("Добавление нового урока");

        // Создаем форму
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> subjectCombo = new ComboBox<>();
        subjectCombo.setPromptText("Выберите предмет");
        subjectCombo.getItems().addAll(scheduleService.getUniqueSubjects());
        subjectCombo.setEditable(true);

        TextField roomField = new TextField();
        roomField.setPromptText("Кабинет");

        ComboBox<String> timeCombo = new ComboBox<>();
        timeCombo.setPromptText("Время начала");
        timeCombo.getItems().addAll(
            "08:30", "09:15", "10:00", "10:45", "11:30", "12:15",
            "13:00", "13:45", "14:30", "15:15", "16:00"
        );

        grid.add(new Label("Предмет:"), 0, 0);
        grid.add(subjectCombo, 1, 0);
        grid.add(new Label("Кабинет:"), 0, 1);
        grid.add(roomField, 1, 1);
        grid.add(new Label("Время:"), 0, 2);
        grid.add(timeCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Добавляем кнопки
        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Валидация
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        subjectCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            saveButton.setDisable(newVal == null || newVal.trim().isEmpty() ||
                                roomField.getText().trim().isEmpty() ||
                                timeCombo.getValue() == null);
        });

        roomField.textProperty().addListener((obs, oldVal, newVal) -> {
            saveButton.setDisable(subjectCombo.getValue() == null ||
                                newVal.trim().isEmpty() ||
                                timeCombo.getValue() == null);
        });

        timeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            saveButton.setDisable(subjectCombo.getValue() == null ||
                                roomField.getText().trim().isEmpty() ||
                                newVal == null);
        });

        // Обработка результата
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Lesson lesson = new Lesson();
                lesson.setSubject(subjectCombo.getValue());
                lesson.setRoom(roomField.getText());
                try {
                    lesson.setStartTime(LocalTime.parse(timeCombo.getValue()));
                } catch (DateTimeParseException e) {
                    showError("Ошибка", "Неверный формат времени");
                    return null;
                }
                lesson.setDayOfWeek(date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("ru")));
                return lesson;
            }
            return null;
        });

        Optional<Lesson> result = dialog.showAndWait();
        result.ifPresent(lesson -> {
            try {
                scheduleService.saveLesson(lesson);
                showSuccess("Успех", "Урок успешно добавлен");
                refreshLessons();
            } catch (Exception e) {
                showError("Ошибка", "Не удалось добавить урок: " + e.getMessage());
            }
        });
    }

    private void showDeleteLessonDialog(Lesson lesson) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Удаление урока");
        alert.setHeaderText("Подтверждение удаления");
        alert.setContentText("Вы уверены, что хотите удалить урок " + lesson.getSubject() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            scheduleService.deleteLesson(Long.valueOf(lesson.getId()));
            refreshCurrentTableView();
        }
    }

    private void updateSchedule() {
        // Удалить метод updateSchedule() и все его вызовы
        // Удалить все обращения к weekView, currentWeekStart, getLessonsByDate
        // Везде, где нужен список уроков на дату, использовать:
        // List<Lesson> dayLessons = scheduleService.getAllLessons().stream()
        //     .filter(lesson -> lesson.getDate() != null && lesson.getDate().equals(date))
        //     .collect(Collectors.toList());
        // Проверить, чтобы не было дублирующих методов createDayBox и createLessonCard
    }

    private VBox createDayBox(LocalDate date) {
        VBox dayBox = new VBox(10);
        dayBox.setPadding(new Insets(10));
        dayBox.setMinWidth(200);
        dayBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e9ecef; -fx-border-width: 1px; -fx-border-radius: 4px;");

        // Заголовок дня
        String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("ru"));
        Label dayLabel = new Label(dayName);
        dayLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        // Дата
        Label dateLabel = new Label(date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        dateLabel.setStyle("-fx-text-fill: #6c757d;");

        // Контейнер для уроков
        VBox lessonsBox = new VBox(5);
        lessonsBox.setPadding(new Insets(5));

        // Получаем уроки для этого дня
        List<Lesson> dayLessons = lessons.stream()
            .filter(lesson -> {
                String lessonDay = lesson.getDayOfWeek();
                return lessonDay != null && lessonDay.equals(dayName);
            })
            .sorted(Comparator.comparing(Lesson::getStartTime))
            .collect(Collectors.toList());

        // Добавляем карточки уроков
        for (Lesson lesson : dayLessons) {
            lessonsBox.getChildren().add(createLessonCard(lesson));
        }

        // Кнопка добавления урока
        Button addButton = new Button("+ Добавить урок");
        addButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 4px;");
        addButton.setOnAction(e -> {
            showAddLessonDialog(date);
            refreshLessons();
        });

        dayBox.getChildren().addAll(dayLabel, dateLabel, lessonsBox, addButton);
        return dayBox;
    }

    private VBox createLessonCard(Lesson lesson) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-width: 1px; -fx-border-radius: 4px;");

        Label subjectLabel = new Label(lesson.getSubject());
        subjectLabel.setStyle("-fx-font-weight: bold;");

        Label timeLabel = new Label(lesson.getStartTime().toString());
        timeLabel.setStyle("-fx-text-fill: #6c757d;");

        Label roomLabel = new Label("Кабинет: " + lesson.getRoom());
        roomLabel.setStyle("-fx-text-fill: #6c757d;");

        HBox buttonBox = new HBox(5);
        Button editButton = new Button("✎");
        Button homeworkButton = new Button("📚");
        Button deleteButton = new Button("×");

        editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 4px;");
        homeworkButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 4px;");
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 4px;");

        editButton.setOnAction(e -> showEditLessonDialog(lesson));
        homeworkButton.setOnAction(e -> showHomeworkDialog(lesson));
        deleteButton.setOnAction(e -> {
            if (showConfirmation("Подтверждение", "Вы уверены, что хотите удалить этот урок?")) {
                try {
                    scheduleService.deleteLesson(lesson.getId());
                    showSuccess("Успех", "Урок успешно удален");
                    refreshLessons();
                } catch (Exception ex) {
                    showError("Ошибка", "Не удалось удалить урок: " + ex.getMessage());
                }
            }
        });

        buttonBox.getChildren().addAll(editButton, homeworkButton, deleteButton);
        card.getChildren().addAll(subjectLabel, timeLabel, roomLabel, buttonBox);

        return card;
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("alert");
        alert.showAndWait();
    }

    private void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private VBox createInitialView() {
        VBox initialView = new VBox(10);
        initialView.getChildren().add(createNavigationHeader());
        initialView.getChildren().add(createWeekView(currentDate));
        return initialView;
    }

    private void refreshLessons() {
        try {
            List<Lesson> allLessons = scheduleService.getAllLessons();
            lessons.clear();
            lessons.addAll(allLessons);
            
            // Обновляем комбобоксы
            List<String> subjects = scheduleService.getUniqueSubjects();
            subjectComboBox.getItems().clear();
            subjectComboBox.getItems().addAll(subjects);
            
            // Обновляем представление
            Platform.runLater(() -> {
                if (currentDate != null) {
                    showWeek(currentDate);
                }
            });
        } catch (Exception e) {
            showError("Ошибка", "Не удалось загрузить расписание: " + e.getMessage());
        }
    }

    private void updateWeekView() {
        if (weekView != null) {
            weekView.getChildren().clear();
            for (int i = 0; i < 7; i++) {
                LocalDate date = weekStart.plusDays(i);
                VBox dayBox = createDayBox(date);
                weekView.getChildren().add(dayBox);
            }
        }
    }


    private String getDefaultDayOfWeek() {
        // Получаем день недели по умолчанию
        String defaultDayOfWeek = "Понедельник";
        
        // Используем статический метод из класса Lesson
        return Lesson.normalizeDayOfWeek(defaultDayOfWeek);
    }
    
    // Метод updateCurrentTableView обновляет текущую таблицу уроков
    private void updateCurrentTableView() {
        if (currentTableView == null) return;
        
        List<Lesson> allLessons = scheduleService.getAllLessons();
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) return;
        
        // Используем статический метод из класса Lesson для нормализации дня недели
        String dayOfWeek = Lesson.normalizeDayOfWeek(selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("ru")));
        
        // Обновляем данные в таблице
        List<Lesson> filteredLessons = allLessons.stream()
            .filter(l -> {
                if (l.getDayOfWeek() == null) return false;
                // Сравниваем нормализованные значения
                boolean match = Lesson.normalizeDayOfWeek(l.getDayOfWeek()).equals(dayOfWeek);
                
                // Добавлена дополнительная проверка для отладки
                if (!match) {
                    System.out.println("Не совпадают дни недели:");
                    System.out.println("- Урок: " + l.getDayOfWeek() + " (исходное)");
                    System.out.println("- Урок: " + Lesson.normalizeDayOfWeek(l.getDayOfWeek()) + " (нормализованное)");
                    System.out.println("- Фильтр: " + dayOfWeek + " (исходное)");
                    System.out.println("- Фильтр: " + Lesson.normalizeDayOfWeek(dayOfWeek) + " (нормализованное)");
                }
                
                return match;
            })
            .toList();
        
        currentTableView.getItems().setAll(filteredLessons);
    }
    
    /**
     * Уведомляет слушателей об изменении данных
     */
    private void fireDataChanged() {
        // Здесь будет реализация уведомления слушателей
        // Эта реализация зависит от конкретной архитектуры приложения
        System.out.println("Данные расписания были изменены");
        
        // Можно добавить обновление интерфейса или отправку событий другим компонентам
        // Например, можно вызвать updateCurrentTableView(), если это уместно
        updateCurrentTableView();
    }
    
    /**
     * Обновляет текущую таблицу уроков
     */
    private void refreshCurrentTableView() {
        if (currentView != null) {
            content.getChildren().clear();
            if (currentView instanceof VBox) {
                content.getChildren().add(currentView);
            } else {
                showWeek(currentDate);
            }
        }
    }

    private void updateCurrentView() {
        if (currentDate == null) return;
        
        System.out.println("Обновление интерфейса для даты: " + currentDate);
        
        // Получаем контейнер
        VBox contentContainer = (VBox) getContent();
        
        // Удаляем старое представление
        Node oldView = contentContainer.getChildren().stream()
            .filter(node -> node instanceof VBox)
            .findFirst()
            .orElse(null);
        
        if (oldView != null) {
            contentContainer.getChildren().remove(oldView);
        }
        
        // Создаем новое представление с актуальными данными
        VBox newView = createDayView(currentDate);
        contentContainer.getChildren().add(newView);
        this.currentView = newView;
    }
    
    /**
     * Показывает диалоговое окно для добавления нового урока
     */
    private void showEditDialog(Lesson lesson, ScheduleService scheduleService) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Редактировать урок");
        dialog.setHeaderText("Введите данные для редактирования урока");

        TextField subjectField = new TextField(lesson.getSubject());
        TextField homeworkField = new TextField();
        TextField roomField = new TextField(lesson.getRoom());

        if (lesson.getHomeworkId() != null && lesson.getHomeworkId() > 0) {
            Optional<Homework> homeworkOpt = homeworkService.findById(Long.valueOf(lesson.getHomeworkId()));
            homeworkOpt.ifPresent(homework -> homeworkField.setText(homework.getTitle()));
        }

        ButtonType addButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setStyle("-fx-padding: 20px; -fx-background-color: white;");

        // Стили для полей ввода
        String fieldStyle = "-fx-padding: 8px; -fx-background-color: #f8f9fa; -fx-border-color: #e9ecef; -fx-border-width: 1px; -fx-border-radius: 4px;";
        subjectField.setStyle(fieldStyle);
        homeworkField.setStyle(fieldStyle);
        roomField.setStyle(fieldStyle);
        
        grid.addRow(0, new Label("Предмет:"), subjectField);
        grid.addRow(1, new Label("Кабинет:"), roomField);
        grid.addRow(2, new Label("Домашнее задание:"), homeworkField);
        
        dialog.getDialogPane().setContent(grid);

        // Стили для кнопок
        dialog.getDialogPane().lookupButton(addButtonType).setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 4px; -fx-padding: 8px 16px;");
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-background-radius: 4px; -fx-padding: 8px 16px;");
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                lesson.setSubject(subjectField.getText());
                String room = roomField.getText();
                if (room == null || room.trim().isEmpty()) {
                    showErrorDialog("Кабинет не может быть пустым");
                    return null;
                }
                lesson.setRoom(room);
                if (!homeworkField.getText().isEmpty()) {
                    Homework homework = new Homework();
                    homework.setTitle(homeworkField.getText());
                    homework.setContent("Домашнее задание по теме: " + subjectField.getText());
                    LocalDate lessonDate = datePicker.getValue();
                    homework.setDueDate(lessonDate != null ? lessonDate : LocalDate.now());
                    homeworkService.save(homework);
                    lesson.setHomeworkId(Long.valueOf(homework.getId()));
                }
                scheduleService.updateLesson(lesson);
                showSuccess("Успех", "Урок успешно обновлён!");
                refreshCurrentTableView();
                return null;
            }
            return null;
        });
        dialog.showAndWait();
    }
    
    /**
     * Создает элементы управления для выбора даты
     */
    private HBox createDateControls() {
        HBox dateControls = new HBox(16);
        dateControls.setAlignment(Pos.CENTER);

        Button prevWeekBtn = new Button("← Неделя назад");
        prevWeekBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 4px; -fx-padding: 8px 16px;");
        prevWeekBtn.setOnAction(e -> {
            LocalDate selectedDate = datePicker.getValue();
            if (selectedDate != null) {
                LocalDate newDate = selectedDate.minusWeeks(1);
                datePicker.setValue(newDate);
                showWeek(newDate);
            }
        });

        Label dateLabel = new Label();
        dateLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 12px;");
        dateLabel.textProperty().bind(datePicker.valueProperty().asString("LLLL yyyy"));

        Button nextWeekBtn = new Button("Неделя вперёд →");
        nextWeekBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 4px; -fx-padding: 8px 16px;");
        nextWeekBtn.setOnAction(e -> {
            LocalDate selectedDate = datePicker.getValue();
            if (selectedDate != null) {
                LocalDate newDate = selectedDate.plusWeeks(1);
                datePicker.setValue(newDate);
                showWeek(newDate);
            }
        });
        
        datePicker.setOnAction(e -> showWeek(datePicker.getValue()));
        dateControls.getChildren().addAll(prevWeekBtn, dateLabel, nextWeekBtn);
        return dateControls;
    }
    
    /**
     * Отображает неделю с заданной даты
     */
    private void showWeek(LocalDate startDate) {
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        
        currentDate = startDate;
        weekStart = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        // Очищаем текущий контент
        content.getChildren().clear();
        
        // Добавляем навигацию
        content.getChildren().add(createNavigationHeader());
        
        // Создаем и добавляем представление недели
        HBox weekView = createWeekView(weekStart);
        content.getChildren().add(weekView);
        
        // Обновляем данные
        refreshLessons();
    }
    
    private void showDay(LocalDate dayDate) {
        // Сохраняем текущую дату
        currentDate = dayDate;
        
        // Создаем новое отображение дня
        Node newView = createDayView(dayDate);
        
        // Приводим getContent() к VBox, так как это контейнер с детьми
        VBox contentContainer = (VBox) getContent();
        
        // Удаляем старое отображение
        if (currentView != null) {
            contentContainer.getChildren().remove(currentView);
        }
        
        // Добавляем новое отображение
        contentContainer.getChildren().add(newView);
        currentView = newView;
    }

    @Override
    public Pane getContent() {
        return content;
    }

    private HBox createWeekView(LocalDate startDate) {
        HBox weekContainer = new HBox(10);
        weekContainer.setPadding(new Insets(10));
        weekContainer.setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-width: 1px; -fx-border-radius: 4px;");

        // Создаем представление для каждого дня недели
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            VBox dayBox = createDayBox(date);
            weekContainer.getChildren().add(dayBox);
        }

        return weekContainer;
    }

    private boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private VBox createDayView(LocalDate date) {
        VBox dayView = new VBox(10);
        dayView.setPadding(new Insets(20));
        dayView.setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-width: 1px; -fx-border-radius: 4px;");

        // Заголовок
        Label titleLabel = new Label(date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("ru")) + 
                                   ", " + date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Таблица уроков
        TableView<Lesson> lessonsTable = new TableView<>();
        lessonsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Lesson, String> timeColumn = new TableColumn<>("Время");
        timeColumn.setCellValueFactory(cellData -> {
            LocalTime time = cellData.getValue().getStartTime();
            return new SimpleObjectProperty<>(time != null ? time.toString() : "");
        });

        TableColumn<Lesson, String> subjectColumn = new TableColumn<>("Предмет");
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));

        TableColumn<Lesson, String> roomColumn = new TableColumn<>("Кабинет");
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("room"));

        lessonsTable.getColumns().addAll(timeColumn, subjectColumn, roomColumn);

        // Фильтруем уроки для выбранного дня
        String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("ru"));
        List<Lesson> dayLessons = lessons.stream()
            .filter(lesson -> {
                String lessonDay = lesson.getDayOfWeek();
                return lessonDay != null && lessonDay.equals(dayName);
            })
            .sorted(Comparator.comparing(Lesson::getStartTime))
            .collect(Collectors.toList());

        lessonsTable.getItems().addAll(dayLessons);

        // Кнопка добавления урока
        Button addButton = new Button("+ Добавить урок");
        addButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 4px;");
        addButton.setOnAction(e -> {
            showAddLessonDialog(date);
            refreshLessons();
        });

        dayView.getChildren().addAll(titleLabel, lessonsTable, addButton);
        return dayView;
    }

    private void showEditLessonDialog(Lesson lesson) {
        Dialog<Lesson> dialog = new Dialog<>();
        dialog.setTitle("Редактирование урока");
        dialog.setHeaderText("Изменение данных урока");

        // Создаем форму
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> subjectCombo = new ComboBox<>();
        subjectCombo.setPromptText("Выберите предмет");
        subjectCombo.getItems().addAll(scheduleService.getUniqueSubjects());
        subjectCombo.setValue(lesson.getSubject());
        subjectCombo.setEditable(true);

        TextField roomField = new TextField(lesson.getRoom());
        roomField.setPromptText("Кабинет");

        ComboBox<String> timeCombo = new ComboBox<>();
        timeCombo.setPromptText("Время начала");
        timeCombo.getItems().addAll(
            "08:30", "09:15", "10:00", "10:45", "11:30", "12:15",
            "13:00", "13:45", "14:30", "15:15", "16:00"
        );
        timeCombo.setValue(lesson.getStartTime().toString());

        grid.add(new Label("Предмет:"), 0, 0);
        grid.add(subjectCombo, 1, 0);
        grid.add(new Label("Кабинет:"), 0, 1);
        grid.add(roomField, 1, 1);
        grid.add(new Label("Время:"), 0, 2);
        grid.add(timeCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Добавляем кнопки
        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Валидация
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        subjectCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            saveButton.setDisable(newVal == null || newVal.trim().isEmpty() ||
                                roomField.getText().trim().isEmpty() ||
                                timeCombo.getValue() == null);
        });

        roomField.textProperty().addListener((obs, oldVal, newVal) -> {
            saveButton.setDisable(subjectCombo.getValue() == null ||
                                newVal.trim().isEmpty() ||
                                timeCombo.getValue() == null);
        });

        timeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            saveButton.setDisable(subjectCombo.getValue() == null ||
                                roomField.getText().trim().isEmpty() ||
                                newVal == null);
        });

        // Обработка результата
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                lesson.setSubject(subjectCombo.getValue());
                lesson.setRoom(roomField.getText());
                try {
                    lesson.setStartTime(LocalTime.parse(timeCombo.getValue()));
                } catch (DateTimeParseException e) {
                    showError("Ошибка", "Неверный формат времени");
                    return null;
                }
                return lesson;
            }
            return null;
        });

        Optional<Lesson> result = dialog.showAndWait();
        result.ifPresent(updatedLesson -> {
            try {
                scheduleService.updateLesson(updatedLesson);
                showSuccess("Успех", "Урок успешно обновлен");
                refreshLessons();
            } catch (Exception e) {
                showError("Ошибка", "Не удалось обновить урок: " + e.getMessage());
            }
        });
    }
}
