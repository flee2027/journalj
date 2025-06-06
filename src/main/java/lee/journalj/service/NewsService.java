package lee.journalj.service;

import lee.journalj.data.model.News;
import lee.journalj.data.repository.NewsRepository;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Сервис для работы с новостями.
 */
public class NewsService {
    private static final Logger logger = Logger.getLogger(NewsService.class.getName());
    private final NewsRepository newsRepo;

    public NewsService(NewsRepository newsRepo) {
        if (newsRepo == null) {
            throw new IllegalArgumentException("NewsRepository cannot be null");
        }
        this.newsRepo = newsRepo;
    }

    /**
     * Получить все новости.
     * @return список всех новостей
     */
    public List<News> getAllNews() {
        try {
            return newsRepo.findAll();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to get all news", e);
            throw new RuntimeException("Failed to get all news", e);
        }
    }

    /**
     * Найти новость по id.
     * @param id идентификатор новости
     * @return Optional с новостью или пустой Optional, если новость не найдена
     * @throws IllegalArgumentException если id == null
     */
    public Optional<News> findNewsById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("News ID cannot be null");
        }
        try {
            return newsRepo.findById(id);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to find news by ID: " + id, e);
            throw new RuntimeException("Failed to find news", e);
        }
    }

    /**
     * Сохранить новость.
     * @param news новость для сохранения
     * @throws IllegalArgumentException если news == null или невалидная
     */
    public void saveNews(News news) {
        validateNews(news);
        try {
            newsRepo.save(news);
            logger.info("News saved successfully: " + news.getId());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to save news", e);
            throw new RuntimeException("Failed to save news", e);
        }
    }

    /**
     * Обновить новость.
     * @param news новость для обновления
     * @throws IllegalArgumentException если news == null или невалидная
     */
    public void updateNews(News news) {
        validateNews(news);
        try {
            newsRepo.update(news);
            logger.info("News updated successfully: " + news.getId());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to update news", e);
            throw new RuntimeException("Failed to update news", e);
        }
    }

    /**
     * Удалить новость по id.
     * @param id идентификатор новости
     * @throws IllegalArgumentException если id == null
     */
    public void deleteNews(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("News ID cannot be null");
        }
        try {
            newsRepo.delete(id);
            logger.info("News deleted successfully: " + id);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to delete news: " + id, e);
            throw new RuntimeException("Failed to delete news", e);
        }
    }

    /**
     * Валидация новости.
     * @param news новость для валидации
     * @throws IllegalArgumentException если news невалидная
     */
    private void validateNews(News news) {
        if (news == null) {
            throw new IllegalArgumentException("News cannot be null");
        }
        if (news.getTitle() == null || news.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("News title cannot be empty");
        }
        if (news.getContent() == null || news.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("News content cannot be empty");
        }
        if (news.getPublicationDate() == null) {
            throw new IllegalArgumentException("News publication date cannot be null");
        }
    }
}