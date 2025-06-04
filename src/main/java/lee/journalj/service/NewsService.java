package lee.journalj.service;


import lee.journalj.data.model.News;
import lee.journalj.data.repository.NewsRepository;
import lee.journalj.data.repository.Implementation.NewsRepositoryImplementation;

import java.util.List;

public class NewsService {
    private final NewsRepository newsRepository = new NewsRepositoryImplementation();

    public List<News> getAllNews() {
        return newsRepository.findAll();
    }

    public void saveNews(News news) {
        newsRepository.save(news);
    }

    public void updateNews(News news) {
        newsRepository.update(news);
    }

    public void deleteNews(int id) {
        newsRepository.delete(id);
    }
}