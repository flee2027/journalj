package lee.journalj.data.repository.implementation;


import lee.journalj.data.model.News;
import lee.journalj.data.repository.NewsRepository;
import lee.journalj.data.util.DatabaseHandler;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NewsRepositoryImplementation implements NewsRepository {

    @Override
    public List<News> findAll() {
        List<News> newsList = new ArrayList<>();
        String sql = "SELECT * FROM news ORDER BY publication_date DESC";

        try (Connection conn = DatabaseHandler.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                News news = mapResultSetToNews(rs);
                newsList.add(news);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch news", e);
        }
        return newsList;
    }

    @Override
    public Optional<News> findById(int id) {
        String sql = "SELECT * FROM news WHERE id = ?";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToNews(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find news by id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public void save(News news) {
        String sql = "INSERT INTO news(title, content, publication_date) VALUES(?,?,?)";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, news.getTitle());
            pstmt.setString(2, news.getContent());
            pstmt.setString(3, news.getPublicationDate().toString());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    news.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save news", e);
        }
    }

    @Override
    public void update(News news) {
        String sql = "UPDATE news SET title = ?, content = ? WHERE id = ?";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, news.getTitle());
            pstmt.setString(2, news.getContent());
            pstmt.setInt(3, news.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update news", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM news WHERE id = ?";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete news", e);
        }
    }

    private News mapResultSetToNews(ResultSet rs) throws SQLException {
        News news = new News();
        news.setId(rs.getInt("id"));
        news.setTitle(rs.getString("title"));
        news.setContent(rs.getString("content"));

        // Парсим дату из строки вручную
        String dateStr = rs.getString("publication_date");
        news.setPublicationDate(LocalDateTime.parse(dateStr));

        return news;
    }

}