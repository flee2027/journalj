package lee.journalj.Data.Repository;


import lee.journalj.Data.Model.News;
import java.util.List;
import java.util.Optional;

public interface NewsRepository {
    List<News> findAll();
    Optional<News> findById(int id);
    void save(News news);
    void update(News news);
    void delete(int id);
}