package lee.journalj.service;


import lee.journalj.data.model.News;
import lee.journalj.data.repository.NewsRepository;
import lee.journalj.data.repository.implementation.NewsRepositoryImplementation;

import java.util.List;

public class NewsService {
    private final NewsRepository newsRepo; // Используем интерфейс вместо реализации

    public NewsService() {
        this.newsRepo = (NewsRepository) new NewsRepositoryImplementation();
    }

    public List<News> getAllNews() {
        return newsRepo.findAll();
    }

    public void saveNews(News news) {
        newsRepo.save(news);
    }

    public void updateNews(News news) {
        newsRepo.update(news);
    }

    public void deleteNews(int id) {
        newsRepo.delete(id);
    }
}