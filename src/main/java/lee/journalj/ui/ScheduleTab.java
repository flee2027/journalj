package lee.journalj.ui;

import javafx.application.Platform;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
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
import javafx.collections.FXCollections; // Добавлен недостающий импорт
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;


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
    private Node currentView;
    private VBox content;
    private final ComboBox<DayOfWeek> dayComboBox;
    private final ComboBox<String> subjectComboBox;
    private final ObservableList<Lesson> lessons;
    private final TableView<Lesson> lessonsTable;
    private static final String VIEW_TYPE_WEEK = "Неделя";
    private static final String VIEW_TYPE_TABLE = "Таблица";
    private static final String[] VIEW_TYPES = {VIEW_TYPE_WEEK, VIEW_TYPE_TABLE};

    private LocalDate currentDate = LocalDate.now();
    private LocalDate weekStart = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    private String currentViewType = VIEW_TYPE_WEEK; // Тип текущего представления по умолчанию
    
    // Для табличного представления
    private TableView<Lesson> tableView;
    private HBox weekView;

    /**
     * Конструктор с внедрением зависимостей.
     */
    public ScheduleTab(ScheduleService scheduleService, HomeworkService homeworkService) {
        // Инициализация сервисов
        this.scheduleService = scheduleService;
        this.homeworkService = homeworkService;

        // Инициализация списка уроков
        this.lessons = FXCollections.observableArrayList();

        // Создаем таблицу уроков
        tableView = new TableView<>();

        dayComboBox = new ComboBox<>();
        subjectComboBox = new ComboBox<>();
        lessonsTable = new TableView<>();
        
        // Создаем контейнер для табличного представления
        currentTableView = new TableView<>();
        currentTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Создаем основной контейнер
        mainContainer = new VBox(10);
        mainContainer.setStyle("-fx-padding: 20px; -fx-background-color: #f8f9fa;");

        // Создаем контейнер для контента
        this.content = new VBox(10);
        content.setStyle("-fx-padding: 20px;");
        VBox initialView = createInitialView();
        content.getChildren().add(initialView);


        // Создаем datePicker
        datePicker = new DatePicker(LocalDate.now());
        datePicker.setShowWeekNumbers(true);
        
        // Устанавливаем обработчик изменения даты
        datePicker.setOnAction(e -> updateCurrentTableView());
        
        // Добавляем контент в основной контейнер
        mainContainer.getChildren().add(content);

        // Инициализируем компоненты
        initialize();
    }

    /**
     * Инициализирует вкладку расписания
     */
    private void initialize() {
        // Настраиваем колонки таблицы
        setupTableColumns();
        tableView.setItems(lessons);
        
        // Устанавливаем обработчик изменения даты
        datePicker.setOnAction(e -> updateCurrentTableView());
        
        // Устанавливаем текущую дату
        currentDate = LocalDate.now();
        weekStart = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        // Создаем начальное представление
        showWeek(currentDate);
        
        // Инициализируем контейнер для табличного представления
        currentTableViewContainer = new VBox(10);
        currentTableViewContainer.setStyle("-fx-padding: 20px;");
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
     * Настраивает колонки таблицы уроков
     */
    private void setupTableColumns() {
        try {
            // Очищаем существующие колонки
            tableView.getColumns().clear();

            // Колонка времени
            TableColumn<Lesson, String> timeColumn = new TableColumn<>("Время");
            timeColumn.setCellValueFactory(cellData -> {
                LocalTime time = cellData.getValue().getStartTime();
                return new SimpleObjectProperty<>(time != null ? formatTime(time) : "");
            });

            // Колонка дня недели
            TableColumn<Lesson, String> dayColumn = new TableColumn<>("День");
            dayColumn.setCellValueFactory(cellData -> {
                String day = cellData.getValue().getDayOfWeek();
                return new SimpleObjectProperty<>(day != null ? day : "");
            });

            // Колонка предмета
            TableColumn<Lesson, String> subjectColumn = new TableColumn<>("Предмет");
            subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));

            // Колонка кабинета
            TableColumn<Lesson, String> roomColumn = new TableColumn<>("Кабинет");
            roomColumn.setCellValueFactory(new PropertyValueFactory<>("room"));

            // Добавляем колонки в таблицу
            tableView.getColumns().addAll(timeColumn, dayColumn, subjectColumn, roomColumn);

            // Устанавливаем политику изменения размера колонок
            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            // Добавляем обработчик сортировки
            tableView.getSortOrder().addListener((ListChangeListener<TableColumn<Lesson, ?>>) change -> {
                if (change.next()) {
                    if (change.wasReplaced()) {
                        sortLessons();
                    }
                }
            });
        } catch (Exception e) {
            showError("Ошибка", "Не удалось настроить колонки таблицы: " + e.getMessage());
        }
    }
    
    private String formatTime(LocalTime time) {
        return time != null ? time.toString() : "";
    }

    private void sortLessons() {
        try {
            List<Lesson> sortedLessons = new ArrayList<>(tableView.getItems());
            sortedLessons.sort((lesson1, lesson2) -> {
                String day1 = Lesson.normalizeDayOfWeek(lesson1.getDayOfWeek());
                String day2 = Lesson.normalizeDayOfWeek(lesson2.getDayOfWeek());

                int dayComparison = 0;
                try {
                    DayOfWeek d1 = DayOfWeek.valueOf(day1.toUpperCase());
                    DayOfWeek d2 = DayOfWeek.valueOf(day2.toUpperCase());
                    dayComparison = Integer.compare(d1.getValue(), d2.getValue());
                } catch (IllegalArgumentException e) {
                    dayComparison = day1.compareTo(day2);
                }

                if (dayComparison == 0 && lesson1.getStartTime() != null && lesson2.getStartTime() != null) {
                    return lesson1.getStartTime().compareTo(lesson2.getStartTime());
                }
                return dayComparison;
            });

            tableView.getItems().setAll(sortedLessons);
        } catch (Exception e) {
            showError("Ошибка", "Не удалось отсортировать уроки: " + e.getMessage());
        }
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
        Node newView;
        if (VIEW_TYPE_WEEK.equals(currentViewType)) {
            newView = createWeekView(weekStart);
        } else {
            newView = createDayView(currentDate);
        }

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

    private HBox createViewSwitcher() {
        HBox viewSwitcher = new HBox(10);
        ComboBox<String> viewComboBox = new ComboBox<>();
        viewComboBox.getItems().addAll(VIEW_TYPES);
        viewComboBox.setValue(currentViewType);

        viewComboBox.setOnAction(e -> {
            currentViewType = viewComboBox.getValue();

            if (VIEW_TYPE_WEEK.equals(currentViewType)) {
                weekView = createWeekView(weekStart);
                VBox parent = (VBox) getContent();
                parent.getChildren().removeIf(node -> node instanceof HBox || node instanceof TableView);
                parent.getChildren().add(weekView);
            } else {
                TableView<Lesson> tableView = createTableView();
                VBox parent = (VBox) getContent();
                parent.getChildren().removeIf(node -> node instanceof HBox || node instanceof TableView);
                parent.getChildren().add(tableView);
            }

            refreshLessons();
        });

        viewSwitcher.getChildren().add(viewComboBox);
        return viewSwitcher;
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
        timeCombo.getItems().addAll(
                "08:00", "08:45", "09:30", "10:15", "11:00",
                "11:45", "12:30", "13:15", "14:00", "14:45", "15:30"
        );
        timeCombo.setPromptText("Время начала");

        ComboBox<String> endTimeCombo = new ComboBox<>();
        endTimeCombo.getItems().addAll(
                "08:45", "09:30", "10:15", "11:00", "11:45",
                "12:30", "13:15", "14:00", "14:45", "15:30", "16:15"
        );
        endTimeCombo.setPromptText("Время окончания");

        grid.add(new Label("Предмет:"), 0, 0);
        grid.add(subjectCombo, 1, 0);
        grid.add(new Label("Кабинет:"), 0, 1);
        grid.add(roomField, 1, 1);
        grid.add(new Label("Время начала:"), 0, 2);
        grid.add(timeCombo, 1, 2);
        grid.add(new Label("Время окончания:"), 0, 3);
        grid.add(endTimeCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);
        ButtonType addButtonType = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Lesson lesson = new Lesson();
                lesson.setSubject(subjectCombo.getValue());
                lesson.setRoom(roomField.getText());

                try {
                    lesson.setStartTime(LocalTime.parse(timeCombo.getValue()));
                    lesson.setEndTime(LocalTime.parse(endTimeCombo.getValue()));
                } catch (DateTimeParseException e) {
                    showError("Ошибка", "Неверный формат времени");
                    return null;
                }

                // Устанавливаем дату и день недели
                lesson.setDate(date);
                lesson.setDayOfWeek(date.getDayOfWeek().getDisplayName(
                        TextStyle.FULL, new Locale("ru")
                ));

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
        dayBox.setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-width: 1px;");

        String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("ru"));
        Label dayLabel = new Label(dayName);
        dayLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label dateLabel = new Label(date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        dateLabel.setStyle("-fx-text-fill: #6c757d;");

        VBox lessonsBox = new VBox(5);
        lessonsBox.setPadding(new Insets(5));

        List<Lesson> dayLessons = lessons.stream()
                .filter(lesson -> lesson.getDate() != null && lesson.getDate().isEqual(date))
                .sorted(Comparator.comparing(Lesson::getStartTime))
                .collect(Collectors.toList());

        lessonsBox.getChildren().clear();
        for (Lesson lesson : dayLessons) {
            lessonsBox.getChildren().add(createLessonCard(lesson));
        }

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

    private HBox createControlPanel() {
        HBox controlPanel = new HBox(10);
        controlPanel.setAlignment(Pos.CENTER_LEFT);

        // Переключатель типов представления
        ComboBox<String> viewTypeComboBox = new ComboBox<>();
        viewTypeComboBox.getItems().addAll(VIEW_TYPES);
        viewTypeComboBox.setValue(currentViewType);
        viewTypeComboBox.setOnAction(e -> {
            currentViewType = viewTypeComboBox.getValue();
            reloadSchedule();
        });

        // Календарь для выбора даты
        datePicker = new DatePicker(LocalDate.now());
        datePicker.setShowWeekNumbers(true);
        datePicker.setOnAction(e -> updateCurrentTableView());

        // Панель с элементами управления
        Label viewLabel = new Label("Представление:");
        viewLabel.setStyle("-fx-font-weight: bold;");

        controlPanel.getChildren().addAll(viewLabel, viewTypeComboBox, datePicker);
        return controlPanel;
    }

    
    /**
     * Создает табличное представление уроков
     */
    private TableView<Lesson> createTableView() {
        TableView<Lesson> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Настройка колонок таблицы
        setupTableColumns();
        addTableColumns();
        tableView.setItems(lessons);

        // Сортируем уроки при инициализации
        sortLessons();

        return tableView;
    }

    
    /**
     * Обновляет текущее табличное представление с фильтрацией по выбранной дате
     */
    private void updateCurrentTableView() {
        try {
            LocalDate selectedDate = datePicker.getValue();
            if (selectedDate == null) return;

            List<Lesson> filteredLessons = lessons.stream()
                    .filter(lesson -> lesson.getDate() != null && lesson.getDate().isEqual(selectedDate))
                    .sorted(Comparator.comparing(Lesson::getStartTime))
                    .collect(Collectors.toList());

            tableView.getItems().setAll(filteredLessons);
        } catch (Exception e) {
            showError("Ошибка", "Не удалось обновить таблицу: " + e.getMessage());
        }
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

    private void refreshLessons() {
        try {
            List<Lesson> allLessons = scheduleService.getAllLessons();
            System.out.println("Загружено уроков: " + allLessons.size());
            lessons.clear();
            lessons.addAll(allLessons);

            Platform.runLater(() -> {
                if (VIEW_TYPE_WEEK.equals(currentViewType)) {
                    updateWeekView();
                } else {
                    updateTableView();
                }
            });
        } catch (Exception e) {
            showError("Ошибка", "Не удалось загрузить расписание: " + e.getMessage());
        }
    }

    private void updateWeekView() {
        try {
            if (weekView != null) {
                weekView.getChildren().clear(); // Очищаем старые дни

                for (int i = 0; i < 7; i++) {
                    LocalDate date = weekStart.plusDays(i);
                    VBox dayBox = createDayBox(date);
                    weekView.getChildren().add(dayBox);
                }
            }
        } catch (Exception e) {
            showError("Ошибка", "Не удалось обновить недельное представление: " + e.getMessage());
        }
    }



    private String getDefaultDayOfWeek() {
        // Получаем день недели по умолчанию
        String defaultDayOfWeek = "Понедельник";
        
        // Используем статический метод из класса Lesson
        return Lesson.normalizeDayOfWeek(defaultDayOfWeek);
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
        try {
            if (startDate == null) startDate = LocalDate.now();
            currentDate = startDate;
            weekStart = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            // Очищаем только weekView, не трогая controlPanel
            if (weekView != null) {
                weekView.getChildren().clear();
                weekView.getChildren().addAll(createWeekView(weekStart).getChildren());
            } else {
                weekView = createWeekView(weekStart);
                VBox initialView = (VBox) getContent();
                initialView.getChildren().add(weekView);
            }

            refreshLessons();

        } catch (Exception e) {
            showError("Ошибка", "Не удалось показать неделю: " + e.getMessage());
        }
    }
    
    // Метод для установки текущего типа представления
    public void setViewType(String viewType) {
        this.currentViewType = viewType;
        
        // Обновляем представление при смене типа
        showWeek(currentDate);
    }
    
    // Метод для получения текущего типа представления
    public String getViewType() {
        return currentViewType;
    }



    private VBox createInitialView() {
        VBox initialView = new VBox(10);
        initialView.getChildren().add(createNavigationHeader());

        // Панель управления (включая кнопки переключения недель)
        HBox controlPanel = createNavigationControls(); // Получаем HBox с кнопками
        initialView.getChildren().add(controlPanel); // Добавляем в интерфейс

        // Добавляем недельное представление
        if (VIEW_TYPE_WEEK.equals(currentViewType)) {
            weekView = createWeekView(weekStart);
            initialView.getChildren().add(weekView);
        } else {
            initialView.getChildren().add(createTableView());
        }

        return initialView;
    }

    private HBox createNavigationControls() {
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER);

        Button prevWeekBtn = new Button("← Неделя назад");
        Button nextWeekBtn = new Button("Следующая неделя →");

        prevWeekBtn.setOnAction(e -> {
            currentDate = currentDate.minusWeeks(1);
            showWeek(currentDate);
        });

        nextWeekBtn.setOnAction(e -> {
            currentDate = currentDate.plusWeeks(1);
            showWeek(currentDate);
        });

        controls.getChildren().addAll(prevWeekBtn, nextWeekBtn);
        return controls;
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

    private HBox createWeekView(LocalDate weekStart) {
        HBox weekBox = new HBox(10);
        weekBox.setPadding(new Insets(10));
        weekBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e9ecef; -fx-border-width: 1px;");

        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            VBox dayBox = createDayBox(date);
            if (dayBox != null) {
                weekBox.getChildren().add(dayBox);
            }
        }

        return weekBox;
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

        // Заголовок с датой и днем недели
        String normalizedDayOfWeek = Lesson.normalizeDayOfWeek(date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("ru")));
        String formattedDate = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        Label titleLabel = new Label(normalizedDayOfWeek + ", " + formattedDate);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Таблица уроков
        TableView<Lesson> lessonsTable = new TableView<>();
        lessonsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Колонка времени
        TableColumn<Lesson, String> timeColumn = new TableColumn<>("Время");
        timeColumn.setCellValueFactory(cellData -> {
            LocalTime time = cellData.getValue().getStartTime();
            return new SimpleObjectProperty<>(time != null ? formatTime(time) : "");
        });

        // Колонка предмета
        TableColumn<Lesson, String> subjectColumn = new TableColumn<>("Предмет");
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));

        // Колонка кабинета
        TableColumn<Lesson, String> roomColumn = new TableColumn<>("Кабинет");
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("room"));

        // Добавляем колонки в таблицу
        lessonsTable.getColumns().addAll(timeColumn, subjectColumn, roomColumn);

        // Фильтруем уроки для выбранного дня
        List<Lesson> dayLessons = lessons.stream()
                .filter(lesson -> {
                    String lessonDay = lesson.getDayOfWeek();
                    return lessonDay != null &&
                            Lesson.normalizeDayOfWeek(lessonDay).equals(Lesson.normalizeDayOfWeek(normalizedDayOfWeek));
                })
                .sorted(Comparator.comparing(Lesson::getStartTime))
                .collect(Collectors.toList());

        lessonsTable.getItems().setAll(dayLessons);

        // Кнопка добавления урока
        Button addButton = new Button("+ Добавить урок");
        addButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 4px;");
        addButton.setOnAction(e -> showAddLessonDialog(date));

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

    private void addTableColumns() {
        // Колонка времени
        TableColumn<Lesson, String> timeColumn = new TableColumn<>("Время");
        timeColumn.setCellValueFactory(cellData -> {
            LocalTime time = cellData.getValue().getStartTime();
            return new SimpleObjectProperty<>(time != null ? time.toString() : "");
        });

        // Колонка дня недели
        TableColumn<Lesson, String> dayColumn = new TableColumn<>("День");
        dayColumn.setCellValueFactory(cellData -> {
            String day = cellData.getValue().getDayOfWeek();
            return new SimpleObjectProperty<>(day != null ? day : "");
        });

        // Колонка предмета
        TableColumn<Lesson, String> subjectColumn = new TableColumn<>("Предмет");
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));

        // Колонка кабинета
        TableColumn<Lesson, String> roomColumn = new TableColumn<>("Кабинет");
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("room"));

        // Добавляем колонки в таблицу
        tableView.getColumns().addAll(timeColumn, dayColumn, subjectColumn, roomColumn);
    }


    private void updateTableView() {
        try {
            LocalDate selectedDate = datePicker.getValue();
            if (selectedDate == null) return;

            // Фильтруем уроки по конкретной дате
            List<Lesson> filteredLessons = lessons.stream()
                    .filter(lesson -> lesson.getDate() != null && lesson.getDate().isEqual(selectedDate))
                    .sorted(Comparator.comparing(Lesson::getStartTime))
                    .collect(Collectors.toList());

            tableView.getItems().setAll(filteredLessons);
        } catch (Exception e) {
            showError("Ошибка", "Не удалось обновить таблицу: " + e.getMessage());
        }
    }
    }
