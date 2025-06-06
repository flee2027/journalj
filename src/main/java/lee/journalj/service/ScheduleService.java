package lee.journalj.service;

import lee.journalj.data.model.Homework;
import lee.journalj.data.model.Lesson;
import lee.journalj.data.repository.HomeworkRepository;
import lee.journalj.data.repository.LessonRepository;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Сервис для работы с расписанием и уроками.
 */
public class ScheduleService {
    private static final Logger logger = Logger.getLogger(ScheduleService.class.getName());
    private final LessonRepository lessonRepo;
    private final HomeworkRepository homeworkRepo;
    private final Set<Consumer<List<String>>> subjectChangeListeners;

    public ScheduleService(LessonRepository lessonRepo, HomeworkRepository homeworkRepo) {
        if (lessonRepo == null || homeworkRepo == null) {
            throw new IllegalArgumentException("Repositories cannot be null");
        }
        this.lessonRepo = lessonRepo;
        this.homeworkRepo = homeworkRepo;
        this.subjectChangeListeners = new HashSet<>();
    }

    /**
     * Добавить слушатель изменений списка предметов.
     * @param listener слушатель изменений
     */
    public void addSubjectChangeListener(Consumer<List<String>> listener) {
        if (listener != null) {
            subjectChangeListeners.add(listener);
        }
    }

    /**
     * Удалить слушатель изменений списка предметов.
     * @param listener слушатель изменений
     */
    public void removeSubjectChangeListener(Consumer<List<String>> listener) {
        if (listener != null) {
            subjectChangeListeners.remove(listener);
        }
    }

    /**
     * Уведомить всех слушателей об изменении списка предметов.
     */
    private void notifySubjectChangeListeners() {
        List<String> subjects = getUniqueSubjects();
        for (Consumer<List<String>> listener : subjectChangeListeners) {
            try {
                listener.accept(subjects);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error notifying subject change listener", e);
            }
        }
    }

    /**
     * Получить все уроки.
     * @return список всех уроков
     */
    public List<Lesson> getAllLessons() {
        try {
            return lessonRepo.findAll();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to get all lessons", e);
            throw new RuntimeException("Failed to get all lessons", e);
        }
    }

    /**
     * Получить список уникальных предметов из расписания.
     * @return список уникальных предметов
     */
    public List<String> getUniqueSubjects() {
        try {
            return getAllLessons().stream()
                .map(Lesson::getSubject)
                .filter(subject -> subject != null && !subject.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to get unique subjects", e);
            throw new RuntimeException("Failed to get unique subjects", e);
        }
    }

    /**
     * Сохранить урок.
     * @param lesson урок для сохранения
     * @throws IllegalArgumentException если lesson == null
     */
    public void saveLesson(Lesson lesson) {
        validateLesson(lesson);
        try {
            lessonRepo.save(lesson);
            logger.info("Lesson saved successfully: " + lesson.getId());
            notifySubjectChangeListeners();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to save lesson", e);
            throw new RuntimeException("Failed to save lesson", e);
        }
    }

    /**
     * Обновить урок.
     * @param lesson урок для обновления
     * @throws IllegalArgumentException если lesson == null
     */
    public void updateLesson(Lesson lesson) {
        validateLesson(lesson);
        try {
            lessonRepo.update(lesson);
            logger.info("Lesson updated successfully: " + lesson.getId());
            notifySubjectChangeListeners();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to update lesson", e);
            throw new RuntimeException("Failed to update lesson", e);
        }
    }

    /**
     * Удалить урок по id.
     * @param id идентификатор урока
     */
    public void deleteLesson(Long id) {
        try {
            lessonRepo.delete(id);
            logger.info("Lesson deleted successfully: " + id);
            notifySubjectChangeListeners();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to delete lesson", e);
            throw new RuntimeException("Failed to delete lesson", e);
        }
    }

    /**
     * Сохранить домашнее задание.
     * @param homework домашнее задание для сохранения
     * @throws IllegalArgumentException если homework == null
     */
    public void saveHomework(Homework homework) {
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
     * Получить домашнее задание по id.
     * @param id идентификатор домашнего задания
     * @return Optional с домашним заданием или пустой Optional, если задание не найдено
     */
    public Optional<Homework> getHomeworkById(Long id) {
        try {
            return homeworkRepo.findById(id);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to get homework by id: " + id, e);
            throw new RuntimeException("Failed to get homework by id", e);
        }
    }

    private void validateLesson(Lesson lesson) {
        if (lesson == null) {
            throw new IllegalArgumentException("Lesson cannot be null");
        }
        if (lesson.getSubject() == null || lesson.getSubject().trim().isEmpty()) {
            throw new IllegalArgumentException("Lesson subject cannot be empty");
        }
        if (lesson.getRoom() == null || lesson.getRoom().trim().isEmpty()) {
            throw new IllegalArgumentException("Lesson room cannot be empty");
        }
    }

    private void validateHomework(Homework homework) {
        if (homework == null) {
            throw new IllegalArgumentException("Homework cannot be null");
        }
        if (homework.getTitle() == null || homework.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Homework title cannot be empty");
        }
    }
}