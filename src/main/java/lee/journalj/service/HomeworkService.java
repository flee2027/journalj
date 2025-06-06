package lee.journalj.service;

import lee.journalj.data.model.Homework;
import lee.journalj.data.repository.HomeworkRepository;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Сервис для работы с домашними заданиями.
 */
public class HomeworkService {
    private static final Logger logger = Logger.getLogger(HomeworkService.class.getName());
    private final HomeworkRepository homeworkRepo;

    public HomeworkService(HomeworkRepository homeworkRepo) {
        if (homeworkRepo == null) {
            throw new IllegalArgumentException("HomeworkRepository cannot be null");
        }
        this.homeworkRepo = homeworkRepo;
    }

    /**
     * Найти домашнее задание по id.
     * @param id идентификатор домашнего задания
     * @return Optional с домашним заданием или пустой Optional, если задание не найдено
     * @throws IllegalArgumentException если id == null
     */
    public Optional<Homework> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Homework ID cannot be null");
        }
        try {
            return homeworkRepo.findById(id);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to find homework by ID: " + id, e);
            throw new RuntimeException("Failed to find homework", e);
        }
    }

    /**
     * Сохранить домашнее задание.
     * @param homework домашнее задание для сохранения
     * @throws IllegalArgumentException если homework == null или невалидное
     */
    public void save(Homework homework) {
        validateHomework(homework);
        try {
            homeworkRepo.save(homework);
            logger.info("Homework saved successfully: " + homework.getId());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to save homework", e);
            throw new RuntimeException("Failed to save homework", e);
        }
    }

    /**
     * Обновить домашнее задание.
     * @param homework домашнее задание для обновления
     * @throws IllegalArgumentException если homework == null или невалидное
     */
    public void update(Homework homework) {
        validateHomework(homework);
        try {
            homeworkRepo.update(homework);
            logger.info("Homework updated successfully: " + homework.getId());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to update homework", e);
            throw new RuntimeException("Failed to update homework", e);
        }
    }

    /**
     * Получить все домашние задания.
     * @return список всех домашних заданий
     */
    public List<Homework> getAllHomework() {
        try {
            return homeworkRepo.findAll();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to get all homework", e);
            throw new RuntimeException("Failed to get all homework", e);
        }
    }

    /**
     * Валидация домашнего задания.
     * @param homework домашнее задание для валидации
     * @throws IllegalArgumentException если homework невалидное
     */
    private void validateHomework(Homework homework) {
        if (homework == null) {
            throw new IllegalArgumentException("Homework cannot be null");
        }
        if (homework.getTitle() == null || homework.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Homework title cannot be empty");
        }
        if (homework.getContent() == null) {
            throw new IllegalArgumentException("Homework content cannot be null");
        }
        if (homework.getDueDate() == null) {
            throw new IllegalArgumentException("Homework due date cannot be null");
        }
        if (homework.getLessonId() == null) {
            throw new IllegalArgumentException("Homework lesson ID cannot be null");
        }
    }
}