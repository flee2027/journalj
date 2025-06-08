package lee.journalj.data.repository;

import lee.journalj.data.model.Lesson;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для работы с репозиторием расписания.
 */
public interface ScheduleRepository {
    /**
     * Сохраняет урок в репозитории.
     * @param lesson урок для сохранения
     * @return сохраненный урок
     */
    Lesson save(Lesson lesson);

    /**
     * Обновляет урок в репозитории.
     * @param lesson урок для обновления
     */
    void update(Lesson lesson);

    /**
     * Удаляет урок из репозитория.
     * @param id идентификатор урока
     */
    void delete(Long id);

    /**
     * Находит урок по идентификатору.
     * @param id идентификатор урока
     * @return Optional с уроком, если найден
     */
    Optional<Lesson> findById(Long id);

    /**
     * Получает все уроки из репозитория.
     * @return список всех уроков
     */
    List<Lesson> findAll();
} 