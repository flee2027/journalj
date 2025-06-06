package lee.journalj.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.beans.property.SimpleStringProperty;
import lee.journalj.data.model.Grade;
import lee.journalj.data.model.Student;
import lee.journalj.service.GradeService;
import lee.journalj.service.StudentService;
import lee.journalj.service.ScheduleService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.scene.layout.Pane;
import javafx.application.Platform;

/**
 * Вкладка для отображения оценок.
 */
public class GradesTab implements TabContent {
    private final VBox content;
    private final GradeService gradeService;
    private final StudentService studentService;
    private final ScheduleService scheduleService;
    private final ComboBox<String> subjectComboBox;
    private final TableView<Student> studentsTable;
    private final ObservableList<Student> students;
    private final Map<LocalDate, TableColumn<Student, String>> dateColumns;
    private final DateTimeFormatter dateFormatter;
    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;
    private final Label classAverageLabel;

    /**
     * Конструктор вкладки оценок.
     */
    public GradesTab(GradeService gradeService, StudentService studentService, ScheduleService scheduleService) {
        this.gradeService = gradeService;
        this.studentService = studentService;
        this.scheduleService = scheduleService;
        this.content = new VBox(10);
        this.students = FXCollections.observableArrayList();
        this.dateColumns = new HashMap<>();
        this.dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        this.startDatePicker = new DatePicker(LocalDate.now().minusMonths(1));
        this.endDatePicker = new DatePicker(LocalDate.now());
        this.classAverageLabel = new Label();
        
        // Инициализация компонентов
        this.subjectComboBox = new ComboBox<>();
        this.studentsTable = new TableView<>(students);
        
        // Добавляем слушатель изменений списка предметов
        scheduleService.addSubjectChangeListener(this::updateSubjects);
        
        initializeContent();
    }

    private void initializeContent() {
        content.setPadding(new Insets(20));
        
        // Заголовок
        Label heading = new Label("Журнал оценок");
        heading.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        content.getChildren().add(heading);

        // Панель фильтров
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        
        // Выбор предмета
        Label subjectLabel = new Label("Предмет:");
        subjectComboBox.setPromptText("Выберите предмет");
        subjectComboBox.setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-width: 1px; -fx-border-radius: 4px; -fx-padding: 8px;");
        subjectComboBox.setOnAction(e -> updateGradesTable());
        
        // Фильтр по датам
        Label dateRangeLabel = new Label("Фильтр по датам:");
        startDatePicker.setPromptText("От");
        endDatePicker.setPromptText("До");
        startDatePicker.setOnAction(e -> updateGradesTable());
        endDatePicker.setOnAction(e -> updateGradesTable());
        
        filterBox.getChildren().addAll(subjectLabel, subjectComboBox, dateRangeLabel, startDatePicker, endDatePicker);
        content.getChildren().add(filterBox);

        // Таблица учеников
        studentsTable.setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-width: 1px; -fx-border-radius: 4px;");
        
        // Колонка с ФИО
        TableColumn<Student, String> nameColumn = new TableColumn<>("Ученик");
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFullName()));
        nameColumn.setStyle("-fx-font-weight: bold;");
        studentsTable.getColumns().add(nameColumn);

        // Кнопки управления
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        
        Button addStudentButton = createStyledButton("Добавить ученика", "#2196F3", e -> showAddStudentDialog());
        Button addGradeButton = createStyledButton("Добавить оценку", "#4CAF50", e -> showAddGradeDialog());
        Button exportButton = createStyledButton("Экспорт в Excel", "#FF9800", e -> exportToExcel());

        buttonBox.getChildren().addAll(addStudentButton, addGradeButton, exportButton);
        content.getChildren().add(buttonBox);

        // Статистика класса
        classAverageLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        content.getChildren().add(classAverageLabel);

        content.getChildren().add(studentsTable);

        // Загрузка данных
        loadStudents();
        loadSubjects();
    }

    private Button createStyledButton(String text, String color, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button(text);
        button.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 4px; -fx-padding: 8px 16px;", color));
        button.setOnAction(handler);
        return button;
    }

    private void updateGradesTable() {
        String selectedSubject = subjectComboBox.getValue();
        if (selectedSubject == null) return;

        // Очищаем существующие колонки с датами
        studentsTable.getColumns().clear();
        dateColumns.clear();

        // Добавляем колонку с ФИО
        TableColumn<Student, String> nameColumn = new TableColumn<>("Ученик");
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFullName()));
        nameColumn.setStyle("-fx-font-weight: bold;");
        studentsTable.getColumns().add(nameColumn);

        // Добавляем колонки с датами
        List<LocalDate> dates = gradeService.getUniqueDatesForSubject(selectedSubject).stream()
            .filter(date -> !date.isBefore(startDatePicker.getValue()) && !date.isAfter(endDatePicker.getValue()))
            .sorted()
            .toList();
            
        Map<Long, Map<LocalDate, Grade>> grades = gradeService.getGradesBySubject(selectedSubject);

        for (LocalDate date : dates) {
            TableColumn<Student, String> dateColumn = new TableColumn<>(date.format(dateFormatter));
            dateColumn.setCellValueFactory(cellData -> {
                Student student = cellData.getValue();
                Map<LocalDate, Grade> studentGrades = grades.getOrDefault(student.getId(), new HashMap<>());
                Grade grade = studentGrades.get(date);
                return new SimpleStringProperty(grade != null ? String.valueOf(grade.getValue()) : "");
            });
            dateColumn.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.trim().isEmpty()) {
                        setText(null);
                        setStyle("");
                    } else {
                        try {
                            int value = Integer.parseInt(item);
                            setText(String.valueOf(value));
                            setStyle(getGradeStyle(value));
                        } catch (NumberFormatException e) {
                            setText(null);
                            setStyle("");
                        }
                    }
                }
            });
            dateColumns.put(date, dateColumn);
            studentsTable.getColumns().add(dateColumn);
        }

        // Добавляем колонку со средним баллом
        TableColumn<Student, String> averageColumn = new TableColumn<>("Средний");
        averageColumn.setCellValueFactory(cellData -> {
            Student student = cellData.getValue();
            double average = gradeService.calculateAverageGrade(student.getId(), selectedSubject);
            return new SimpleStringProperty(String.format("%.2f", average));
        });
        averageColumn.setStyle("-fx-font-weight: bold;");
        studentsTable.getColumns().add(averageColumn);

        // Обновляем статистику класса
        updateClassStatistics(selectedSubject);
    }

    private String getGradeStyle(int value) {
        return switch (value) {
            case 5 -> "-fx-text-fill: green; -fx-font-weight: bold;";
            case 4 -> "-fx-text-fill: blue; -fx-font-weight: bold;";
            case 3 -> "-fx-text-fill: orange; -fx-font-weight: bold;";
            case 2 -> "-fx-text-fill: red; -fx-font-weight: bold;";
            default -> "";
        };
    }

    private void updateClassStatistics(String subject) {
        double classAverage = students.stream()
            .mapToDouble(student -> gradeService.calculateAverageGrade(student.getId(), subject))
            .average()
            .orElse(0.0);
        classAverageLabel.setText(String.format("Статистика: Средний балл по классу: %.2f", classAverage));
    }

    private void showAddGradeDialog() {
        String selectedSubject = subjectComboBox.getValue();
        if (selectedSubject == null) {
            showAlert("Ошибка", "Выберите предмет", Alert.AlertType.ERROR);
            return;
        }

        Dialog<Grade> dialog = new Dialog<>();
        dialog.setTitle("Добавить оценку");
        dialog.setHeaderText("Введите данные оценки");

        ComboBox<Student> studentComboBox = new ComboBox<>(students);
        studentComboBox.setPromptText("Выберите ученика");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField valueField = new TextField();
        TextField commentField = new TextField();

        GridPane grid = createInputGrid(
            new Label("Ученик:"), studentComboBox,
            new Label("Дата:"), datePicker,
            new Label("Оценка:"), valueField,
            new Label("Комментарий:"), commentField
        );

        dialog.getDialogPane().setContent(grid);

        ButtonType addButtonType = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    int value = Integer.parseInt(valueField.getText());
                    if (value < 2 || value > 5) {
                        showAlert("Ошибка", "Оценка должна быть от 2 до 5", Alert.AlertType.ERROR);
                        return null;
                    }
                    if (studentComboBox.getValue() == null) {
                        showAlert("Ошибка", "Выберите ученика", Alert.AlertType.ERROR);
                        return null;
                    }
                    Grade grade = new Grade();
                    grade.setStudentId(studentComboBox.getValue().getId());
                    grade.setSubject(selectedSubject);
                    grade.setDate(datePicker.getValue());
                    grade.setValue(value);
                    grade.setComment(commentField.getText());
                    return grade;
                } catch (NumberFormatException e) {
                    showAlert("Ошибка", "Введите корректную оценку", Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });

        Optional<Grade> result = dialog.showAndWait();
        result.ifPresent(grade -> {
            try {
                gradeService.saveGrade(grade);
                updateGradesTable();
            } catch (Exception e) {
                showAlert("Ошибка", "Не удалось сохранить оценку: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private GridPane createInputGrid(Object... labelsAndControls) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white;");

        for (int i = 0; i < labelsAndControls.length; i += 2) {
            grid.add((Label) labelsAndControls[i], 0, i/2);
            grid.add((Control) labelsAndControls[i+1], 1, i/2);
        }
        return grid;
    }

    private void exportToExcel() {
        String selectedSubject = subjectComboBox.getValue();
        if (selectedSubject == null) {
            showAlert("Ошибка", "Выберите предмет", Alert.AlertType.ERROR);
            return;
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(selectedSubject);

            CellStyle headerStyle = createHeaderStyle(workbook);

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ФИО");
            headerRow.getCell(0).setCellStyle(headerStyle);

            List<LocalDate> dates = gradeService.getUniqueDatesForSubject(selectedSubject);
            for (int i = 0; i < dates.size(); i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i + 1);
                cell.setCellValue(dates.get(i).format(dateFormatter));
                cell.setCellStyle(headerStyle);
            }
            headerRow.createCell(dates.size() + 1).setCellValue("Средний балл");
            headerRow.getCell(dates.size() + 1).setCellStyle(headerStyle);

            Map<Long, Map<LocalDate, Grade>> grades = gradeService.getGradesBySubject(selectedSubject);
            int rowNum = 1;
            for (Student student : students) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(student.getFullName());

                Map<LocalDate, Grade> studentGrades = grades.getOrDefault(student.getId(), new HashMap<>());
                for (int i = 0; i < dates.size(); i++) {
                    Grade grade = studentGrades.get(dates.get(i));
                    row.createCell(i + 1).setCellValue(grade != null ? grade.getValue() : 0);
                }

                double average = gradeService.calculateAverageGrade(student.getId(), selectedSubject);
                row.createCell(dates.size() + 1).setCellValue(average);
            }

            for (int i = 0; i < dates.size() + 2; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(selectedSubject + "_grades.xlsx")) {
                workbook.write(fileOut);
            }

            showAlert("Успех", "Файл успешно экспортирован", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось экспортировать файл: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        return headerStyle;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void loadStudents() {
        try {
            students.clear();
            students.addAll(studentService.findAllStudents());
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось загрузить список учеников: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadSubjects() {
        try {
            subjectComboBox.getItems().clear();
            subjectComboBox.getItems().addAll(scheduleService.getUniqueSubjects());
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось загрузить список предметов: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAddStudentDialog() {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle("Добавить ученика");
        dialog.setHeaderText("Введите данные ученика");

        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        TextField middleNameField = new TextField();

        GridPane grid = createInputGrid(
            new Label("Фамилия:"), lastNameField,
            new Label("Имя:"), firstNameField,
            new Label("Отчество:"), middleNameField
        );

        dialog.getDialogPane().setContent(grid);

        ButtonType addButtonType = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (lastNameField.getText().trim().isEmpty() || firstNameField.getText().trim().isEmpty()) {
                    showAlert("Ошибка", "Фамилия и имя обязательны для заполнения", Alert.AlertType.ERROR);
                    return null;
                }
                Student student = new Student();
                student.setFirstName(firstNameField.getText().trim());
                student.setLastName(lastNameField.getText().trim());
                student.setMiddleName(middleNameField.getText().trim());
                return student;
            }
            return null;
        });

        Optional<Student> result = dialog.showAndWait();
        result.ifPresent(student -> {
            try {
                studentService.saveStudent(student);
                loadStudents();
            } catch (Exception e) {
                showAlert("Ошибка", "Не удалось сохранить ученика: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void updateSubjects(List<String> subjects) {
        Platform.runLater(() -> {
            String selectedSubject = subjectComboBox.getValue();
            subjectComboBox.getItems().clear();
            subjectComboBox.getItems().addAll(subjects);
            if (selectedSubject != null && subjects.contains(selectedSubject)) {
                subjectComboBox.setValue(selectedSubject);
            }
        });
    }

    @Override
    public Pane getContent() {
        return content;
    }
}