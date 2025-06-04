package lee.journalj.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Node; // Добавлен недостающий импорт
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.scene.web.HTMLEditor;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import lee.journalj.data.model.Homework;
import lee.journalj.data.model.Lesson;
import lee.journalj.data.repository.implementation.LessonRepositoryImplementation;
import lee.journalj.service.HomeworkService;
import lee.journalj.service.ScheduleService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ScheduleTab extends Tab {
    private final VBox content = new VBox(10);
    private final LessonRepositoryImplementation lessonRepo; // Используем конкретную реализацию репозитория уроков
    private final ScheduleService scheduleService;
    private final HomeworkService homeworkService; // Инъекция сервиса домашних заданий
    private LocalDate currentDate = LocalDate.now();
    private VBox currentView; // Сохраняем текущее представление
    private Integer gradeLevel; // Новое поле с типом Integer
    private String className;

    // Существующий конструктор
    public ScheduleTab(ScheduleService scheduleService, HomeworkService homeworkService, LessonRepositoryImplementation lessonRepo) {
        this.scheduleService = scheduleService;
        this.homeworkService = homeworkService;
        this.lessonRepo = lessonRepo;
        setText("Расписание");
        setContent(createInitialView());
    }

    public void addLesson(Lesson lesson) {
        scheduleService.saveLesson(lesson);
        updateCurrentView(currentView); // Обновляем интерфейс
    }

    private HBox createNavigationHeader() {
        HBox navBox = new HBox(10);
        navBox.setStyle("-fx-padding: 10px; -fx-background-color: #f0f0f0;");

        Button prevBtn = new Button("←");
        Button nextBtn = new Button("→");
        Label dateLabel = new Label(currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        prevBtn.setOnAction(e -> {
            currentDate = currentDate.minusWeeks(1);
            updateCurrentView();
            dateLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        });

        nextBtn.setOnAction(e -> {
            currentDate = currentDate.plusWeeks(1);
            updateCurrentView();
            dateLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        });

        navBox.getChildren().addAll(prevBtn, dateLabel, nextBtn);
        return navBox;
    }

    private void editHomework(Lesson lesson) {
        Homework homework = lesson.getHomework();
        if (homework == null) {
            homework = new Homework();
            lesson.setHomework(homework);
        }
        final Homework finalHomework = homework; // Делаем переменную final

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Редактировать домашнее задание");
        HTMLEditor editor = new HTMLEditor();
        editor.setHtmlText(finalHomework.getContent() != null ? finalHomework.getContent() : "<p>Введите текст домашнего задания</p>");

        dialog.getDialogPane().setContent(editor);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> btn == ButtonType.OK ? editor.getHtmlText() : null);

        dialog.showAndWait().ifPresent(result -> {
            if (result != null && !result.isEmpty()) {
                finalHomework.setContent(result);
                lessonRepo.update(lesson.getId(), lesson);
            }
        });
    }

    private void loadWeekViews() {
        List<Lesson> lessons = scheduleService.getAllLessons();
        if (lessons == null) lessons = new ArrayList<>();

        // Переключатель между неделями
        ToggleGroup viewToggleGroup = new ToggleGroup();
        RadioButton weekViewButton = new RadioButton("Неделя");
        RadioButton dayViewButton = new RadioButton("День");
        weekViewButton.setToggleGroup(viewToggleGroup);
        dayViewButton.setToggleGroup(viewToggleGroup);
        weekViewButton.setSelected(true);

        VBox currentView = createWeekView(currentDate);

        viewToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            content.getChildren().remove(1);
            VBox updatedView;
            if (newToggle == weekViewButton) {
                updatedView = createWeekView(currentDate);
            } else {
                updatedView = createDayView(currentDate);
            }
            content.getChildren().add(1, updatedView);
        });

        Button prevBtn = new Button("Предыдущая");
        Button nextBtn = new Button("Следующая");

        prevBtn.setOnAction(e -> {
            this.currentDate = this.currentDate.minusDays(7);
            updateCurrentView(currentView);
        });

        nextBtn.setOnAction(e -> {
            this.currentDate = this.currentDate.plusDays(7);
            updateCurrentView(currentView);
        });

        HBox navigationBar = new HBox(10, weekViewButton, dayViewButton, prevBtn, nextBtn);
        navigationBar.setAlignment(Pos.CENTER);
        content.getChildren().add(0, navigationBar);
        content.getChildren().add(currentView);

        // Кнопка добавления урока
        Button addLessonBtn = new Button("Добавить урок");
        addLessonBtn.setOnAction(e -> showAddLessonDialog());
        content.getChildren().add(addLessonBtn);
    }

    private void addAddLessonButtonToContent() {
        // Удаляем старую кнопку, если она есть
        content.getChildren().removeIf(node -> node instanceof Button && "Добавить урок".equals(((Button) node).getText()));
        
        // Создаем новую кнопку
        Button addLessonBtn = new Button("Добавить урок");
        addLessonBtn.setOnAction(e -> showAddLessonDialog());
        
        // Добавляем кнопку в конец контента
        content.getChildren().add(addLessonBtn);
    }

    private VBox createWeekView(LocalDate date) {
        List<Lesson> updatedLessons = scheduleService.getAllLessons(); // Явно загружаем обновлённый список уроков
        return createWeekView(date, updatedLessons);
    }

    private VBox createWeekView(LocalDate date, List<Lesson> allLessons) {
        VBox weekView = new VBox(10);
        weekView.setStyle("-fx-border-color: #ccc; -fx-padding: 10px;");

        Label weekLabel = new Label("Неделя: " + formatWeek(date));
        weekLabel.setStyle("-fx-font-weight: bold;");
        weekView.getChildren().add(weekLabel);

        HBox daysContainer = new HBox(10);
        daysContainer.setStyle("-fx-padding: 10px;");

        LocalDate startDate = date.with(java.time.DayOfWeek.MONDAY);
        for (int i = 0; i < 7; i++) {
            LocalDate day = startDate.plusDays(i);
            
            // Создаем блок дня с уроками
            VBox dayBox = createDayBox(day, allLessons);
            
            // Отображаем день в формате числа месяца
            Label dayNumberLabel = new Label(String.valueOf(day.getDayOfMonth()));
            dayNumberLabel.setStyle("-fx-font-weight: bold; -fx-alignment: center;");
            
            VBox newDayBox = new VBox(5);
            newDayBox.getChildren().add(dayNumberLabel);
            newDayBox.getChildren().addAll(dayBox.getChildren()); // Добавляем уроки вместе с контентом
            newDayBox.setStyle(dayBox.getStyle());
            
            daysContainer.getChildren().add(newDayBox);
        }

        weekView.getChildren().add(daysContainer);
        return weekView;
    }

    private String formatWeek(LocalDate date) {
        LocalDate startDate = date.with(java.time.DayOfWeek.MONDAY);
        LocalDate endDate = startDate.plusDays(6);
        return startDate.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM")) + 
               " - " + 
               endDate.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

    private VBox createDayBox(LocalDate date, List<Lesson> allLessons) {
        VBox dayBox = new VBox(8);
        dayBox.setStyle("-fx-border-color: #ddd; -fx-padding: 10px; -fx-background-color: #f9f9f9;");

        // Блок с информацией о дне
        VBox headerBox = new VBox(4);
        Label dayOfWeekLabel = new Label(date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
        dayOfWeekLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label dayDateLabel = new Label(date.format(DateTimeFormatter.ofPattern("d MMMM yyyy")));
        dayDateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
        
        headerBox.getChildren().addAll(dayOfWeekLabel, dayDateLabel);
        dayBox.getChildren().add(headerBox);

        // Фильтруем уроки по дате
        List<Lesson> lessonsForDay = allLessons.stream()
            .filter(lesson -> lesson.getDate() != null && lesson.getDate().isEqual(date))
            .toList();

        // Отображаем уроки
        for (Lesson lesson : lessonsForDay) {
            VBox lessonBox = createLessonBox(lesson);
            dayBox.getChildren().add(lessonBox);
        }

        // Кнопка добавления урока на этот день
        Button addLessonBtn = new Button("Добавить урок");
        addLessonBtn.setOnAction(e -> showAddLessonDialog(date)); // Передаем дату дня
        dayBox.getChildren().add(addLessonBtn);
        
        return dayBox;
    }

    private VBox createLessonBox(Lesson lesson) {
        VBox lessonBox = new VBox(4);
        lessonBox.setStyle("-fx-border-color: #eee; -fx-padding: 8px; -fx-background-color: white;");

        Label subjectLabel = new Label("📚 " + lesson.getSubject());
        subjectLabel.setStyle("-fx-font-weight: bold;");

        // Показываем заголовок Д/З, если оно есть
        if (lesson.getHomeworkId() != null && lesson.getHomeworkId() > 0) {
            Label homeworkLabel = new Label("📝 Домашнее задание");
            homeworkLabel.setStyle("-fx-text-fill: blue; -fx-cursor: hand;");
            homeworkLabel.setOnMouseClicked(e -> showHomeworkEditor(lesson.getHomeworkId()));
            lessonBox.getChildren().add(homeworkLabel);
        }

        lessonBox.getChildren().add(subjectLabel);
        return lessonBox;
    }

    private void showAddLessonDialog(LocalDate date) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Добавить урок");
        
        // Создаем поля для ввода данных
        TextField subjectField = new TextField();
        subjectField.setPromptText("Предмет");
        
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(date); // Устанавливаем переданную дату
        
        ChoiceBox<String> dayOfWeekPicker = new ChoiceBox<>(FXCollections.observableArrayList(
            "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"
        ));
        dayOfWeekPicker.setValue(date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
        
        // Поля для времени (пример)
        TextField startTimeField = new TextField("09:00");
        TextField endTimeField = new TextField("10:00");
        
        // Поле для кабинета
        TextField classroomField = new TextField();
        classroomField.setPromptText("Кабинет");
        
        // Формируем интерфейс диалога
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        grid.add(new Label("Предмет:"), 0, 0);
        grid.add(subjectField, 1, 0);
        grid.add(new Label("Дата:"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("День недели:"), 0, 2);
        grid.add(dayOfWeekPicker, 1, 2);
        grid.add(new Label("Время начала:"), 0, 3);
        grid.add(startTimeField, 1, 3);
        grid.add(new Label("Время окончания:"), 0, 4);
        grid.add(endTimeField, 1, 4);
        grid.add(new Label("Кабинет:"), 0, 5);
        grid.add(classroomField, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        // Обработка нажатия на OK
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    LocalTime startTime = LocalTime.parse(startTimeField.getText());
                    LocalTime endTime = LocalTime.parse(endTimeField.getText());
                    
                    Lesson newLesson = new Lesson(
                        0,
                        subjectField.getText(),
                        startTime,
                        endTime,
                        classroomField.getText(),
                        null
                    );
                    
                    newLesson.setDate(datePicker.getValue());
                    newLesson.setDayOfWeek(dayOfWeekPicker.getValue());
                    
                    addLesson(newLesson);
                } catch (DateTimeParseException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка ввода");
                    alert.setHeaderText(null);
                    alert.setContentText("Неверный формат времени. Используйте HH:mm");
                    alert.showAndWait();
                }
            }
            return null;
        });
        
        dialog.setWidth(400);
        dialog.setHeight(300);
        dialog.showAndWait();
    }

    private void showHomeworkEditor(int homeworkId) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Редактировать домашнее задание");
        
        // Получаем домашнее задание из сервиса
        Optional<Homework> homeworkOpt = homeworkService.findById(homeworkId);
        if (homeworkOpt.isEmpty()) return;
        Homework homework = homeworkOpt.get();
        
        // Создаем HTMLEditor
        HTMLEditor htmlEditor = new HTMLEditor();
        htmlEditor.setHtmlText(homework.getContent());
        
        // Сохраняем при нажатии OK
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                homework.setContent(htmlEditor.getHtmlText());
                homeworkService.save(homework);
            }
            return null;
        });
        
        dialog.getDialogPane().setContent(htmlEditor);
        dialog.setWidth(600);
        dialog.setHeight(400);
        dialog.showAndWait();
    }


    private void updateCurrentView(VBox currentView) {
        // Проверяем, какой тип представления сейчас отображается
        ToggleGroup viewToggleGroup = null;
        for (Node node : content.getChildren()) {
            if (node instanceof HBox) {
                for (Node child : ((HBox) node).getChildren()) {
                    if (child instanceof RadioButton && ((RadioButton) child).getText().equals("Неделя")) {
                        viewToggleGroup = ((RadioButton) child).getToggleGroup();
                        break;
                    }
                }
                if (viewToggleGroup != null) break;
            }
        }

        VBox newView;
        if (viewToggleGroup != null && ((RadioButton) viewToggleGroup.getSelectedToggle()).getText().equals("Неделя")) {
            newView = createWeekView(currentDate);
        } else {
            newView = createDayView(currentDate);
        }

        // Заменяем текущее представление на новое с защитой от IndexOutOfBoundsException
        int index = content.getChildren().indexOf(currentView);
        if (index != -1) {
            content.getChildren().set(index, newView);
        } else {
            content.getChildren().add(newView);
        }
        
        // Обновляем кнопку 'Добавить урок'
        addAddLessonButtonToContent();
    }

    private VBox createDayView(LocalDate date) {
        List<Lesson> updatedLessons = scheduleService.getAllLessons(); // Явно загружаем обновлённый список уроков
        VBox dayView = createDayView(date, updatedLessons);
        
        // Добавляем/обновляем кнопку 'Добавить урок'
        addAddLessonButtonToContent();
        
        return dayView;
    }

    private VBox createDayView(LocalDate date, List<Lesson> allLessons) {
        VBox dayView = new VBox(10);
        dayView.setStyle("-fx-border-color: #ccc; -fx-padding: 10px;");

        Label dayLabel = new Label(date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")));
        dayLabel.setStyle("-fx-font-weight: bold;");
        dayView.getChildren().add(dayLabel);

        VBox lessonsBox = new VBox(5);
        for (Lesson lesson : allLessons) {
            if (lesson.getDate() != null && lesson.getDate().isEqual(date)) {
                VBox lessonBox = createLessonBox(lesson);
                lessonsBox.getChildren().add(lessonBox);
            }
        }

        dayView.getChildren().add(lessonsBox);
        return dayView;
    }

    private void showAddLessonDialog() {
        Dialog<Lesson> dialog = new Dialog<>();
        dialog.setTitle("Добавить урок");

        TextField subjectField = new TextField();
        DatePicker datePicker = new DatePicker(currentDate);
        
        // Добавляем выбор дня недели
        ComboBox<String> dayOfWeekPicker = new ComboBox<>();
        dayOfWeekPicker.getItems().addAll(
            "Понедельник", "Вторник", "Среда",
            "Четверг", "Пятница", "Суббота", "Воскресенье"
        );
        dayOfWeekPicker.setValue(currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                Lesson newLesson = new Lesson(0, subjectField.getText(), LocalTime.now(), LocalTime.now().plusHours(1), "Кабинет 1", null);
                newLesson.setDate(datePicker.getValue());
                newLesson.setDayOfWeek(dayOfWeekPicker.getValue()); // Устанавливаем день недели
                addLesson(newLesson);
            }
            return null;
        });

        GridPane grid = new GridPane();
        grid.add(new Label("Предмет:"), 0, 0);
        grid.add(subjectField, 1, 0);
        grid.add(new Label("Дата:"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("День недели:"), 0, 2);
        grid.add(dayOfWeekPicker, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();

        Platform.runLater(this::updateCurrentView);
    }

    private VBox createInitialView() {
        VBox initialView = new VBox(10);
        initialView.getChildren().add(createNavigationHeader());
        initialView.getChildren().add(createWeekView(currentDate));
        return initialView;
    }

    private void updateCurrentView() {
        List<Lesson> updatedLessons = scheduleService.getAllLessons(); // Явно загружаем обновлённый список уроков
        
        // Находим текущее представление (неделя или день)
        for (Node node : content.getChildren()) {
            if (node instanceof VBox) {
                VBox vBox = (VBox) node;
                if (!vBox.getChildren().isEmpty() && vBox.getChildren().get(0) instanceof Label) {
                    VBox currentView = vBox;
                    LocalDate date = currentDate; // Берём текущую дату

                    // Определяем, какой режим отображения используется
                    boolean isWeekMode = false;
                    for (Node navNode : content.getChildren()) {
                        if (navNode instanceof HBox) {
                            for (Node child : ((HBox) navNode).getChildren()) {
                                if (child instanceof RadioButton && ((RadioButton) child).isSelected()) {
                                    isWeekMode = ((RadioButton) child).getText().equals("Неделя");
                                    break;
                                }
                            }
                        }
                    }

                    // Удаляем старое представление
                    int index = content.getChildren().indexOf(currentView);
                    if (index != -1) {
                        content.getChildren().remove(index);

                        // Создаем новое
                        VBox updatedView = isWeekMode ? createWeekView(date, updatedLessons) : createDayView(date, updatedLessons);
                        
                        // Проверяем, что индекс не превышает размер списка
                        if (index < content.getChildren().size()) {
                            content.getChildren().add(index, updatedView);
                        } else {
                            content.getChildren().add(updatedView);
                        }
                    }
                    return; // Выходим из метода после обновления представления
                }
            }
        }
    }

}
