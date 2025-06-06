package lee.journalj.data.repository.implementation;

import lee.journalj.data.model.News;
import lee.journalj.data.repository.BaseRepository;
import lee.journalj.data.repository.NewsRepository;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Реализация репозитория для работы с новостями.
 */
public class NewsRepositoryImplementation extends BaseRepository<News> implements NewsRepository {
    // Форматтер для даты публикации
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Получить все новости.
     */
    @Override
    public List<News> findAll() {
        String sql = "SELECT * FROM news ORDER BY publication_date DESC";
        List<News> newsList = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                newsList.add(mapResultSetToNews(rs));
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch news: " + e.getMessage());
            throw new RuntimeException("Failed to fetch news", e);
        }
        return newsList;
    }

    /**
     * Найти новость по идентификатору.
     */
    @Override
    public Optional<News> findById(Long id) {
        String sql = "SELECT * FROM news WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToNews(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to find news by id: " + id + ", error: " + e.getMessage());
            throw new RuntimeException("Failed to find news by id: " + id, e);
        }
        return Optional.empty();
    }

    /**
     * Сохранить новость.
     */
    @Override
    public News save(News news) {
        if (news.getTitle() == null || news.getTitle().isEmpty()) {
            throw new IllegalArgumentException("News title must not be null or empty");
        }
        String sql = "INSERT INTO news(title, content, publication_date, author) VALUES(?,?,?,?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, news.getTitle());
            pstmt.setString(2, news.getContent());
            pstmt.setString(3, news.getPublicationDate().format(DATE_FORMATTER));
            pstmt.setString(4, news.getAuthor());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    news.setId(rs.getLong(1));
                }
            }
            return news;
        } catch (Exception e) {
            System.err.println("Failed to save news: " + news + ", error: " + e.getMessage());
            throw new RuntimeException("Failed to save news", e);
        }
    }

    /**
     * Обновить новость.
     */
    @Override
    public void update(News news) {
        String sql = "UPDATE news SET title = ?, content = ?, publication_date = ?, author = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, news.getTitle());
            pstmt.setString(2, news.getContent());
            pstmt.setString(3, news.getPublicationDate().format(DATE_FORMATTER));
            pstmt.setString(4, news.getAuthor());
            pstmt.setLong(5, news.getId());
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Failed to update news: " + news + ", error: " + e.getMessage());
            throw new RuntimeException("Failed to update news", e);
        }
    }

    /**
     * Удалить новость.
     */
    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM news WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Failed to delete news: " + id + ", error: " + e.getMessage());
            throw new RuntimeException("Failed to delete news", e);
        }
    }

    /**
     * Маппинг ResultSet в News.
     */
    private News mapResultSetToNews(ResultSet rs) throws SQLException {
        News news = new News();
        news.setId(rs.getLong("id"));
        news.setTitle(rs.getString("title"));
        news.setContent(rs.getString("content"));
        news.setPublicationDate(LocalDateTime.parse(rs.getString("publication_date"), DATE_FORMATTER));
        news.setAuthor(rs.getString("author"));
        return news;
    }
}