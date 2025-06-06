package lee.journalj.data.repository;

import lee.journalj.data.model.Homework;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для работы с репозиторием домашних заданий.
 */
public interface HomeworkRepository {
    /**
     * Сохраняет домашнее задание в репозитории.
     * @param homework домашнее задание для сохранения
     * @return сохраненное домашнее задание
     */
    Homework save(Homework homework);

    /**
     * Обновляет домашнее задание в репозитории.
     * @param homework домашнее задание для обновления
     */
    void update(Homework homework);

    /**
     * Удаляет домашнее задание из репозитория.
     * @param id идентификатор домашнего задания
     */
    void delete(Long id);

    /**
     * Находит домашнее задание по идентификатору.
     * @param id идентификатор домашнего задания
     * @return Optional с домашним заданием, если найдено
     */
    Optional<Homework> findById(Long id);

    /**
     * Получает все домашние задания из репозитория.
     * @return список всех домашних заданий
     */
    List<Homework> findAll();
}