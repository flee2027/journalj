package lee.journalj.data.repository;

import lee.journalj.data.model.News;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для работы с репозиторием новостей.
 */
public interface NewsRepository {
    /**
     * Сохраняет новость в репозитории.
     * @param news новость для сохранения
     * @return сохраненная новость
     */
    News save(News news);

    /**
     * Обновляет новость в репозитории.
     * @param news новость для обновления
     */
    void update(News news);

    /**
     * Удаляет новость из репозитория.
     * @param id идентификатор новости
     */
    void delete(Long id);

    /**
     * Находит новость по идентификатору.
     * @param id идентификатор новости
     * @return Optional с новостью, если найдена
     */
    Optional<News> findById(Long id);

    /**
     * Получает все новости из репозитория.
     * @return список всех новостей
     */
    List<News> findAll();
}