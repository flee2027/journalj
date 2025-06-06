package lee.journalj.data.repository.implementation;

import lee.journalj.data.model.Homework;
import lee.journalj.data.repository.BaseRepository;
import lee.journalj.data.repository.HomeworkRepository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Реализация репозитория домашних заданий.
 */
public class HomeworkRepositoryImplementation extends BaseRepository<Homework> implements HomeworkRepository {

    @Override
    public Homework save(Homework homework) {
        if (homework.getTitle() == null || homework.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Homework title must not be null or empty");
        }
        String sql = "INSERT INTO homework(title, content, due_date, lesson_id) VALUES(?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, homework.getTitle());
            pstmt.setString(2, homework.getContent());
            pstmt.setString(3, homework.getDueDate().toString());
            if (homework.getLessonId() != null) {
                pstmt.setLong(4, homework.getLessonId());
            } else {
                pstmt.setNull(4, java.sql.Types.BIGINT);
            }
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    homework.setId(rs.getLong(1));
                }
            }
            return homework;
        } catch (Exception e) {
            System.err.println("Failed to save homework: " + homework + ", error: " + e.getMessage());
            throw new RuntimeException("Failed to save homework", e);
        }
    }

    @Override
    public void update(Homework homework) {
        String sql = "UPDATE homework SET title = ?, content = ?, due_date = ?, lesson_id = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, homework.getTitle());
            pstmt.setString(2, homework.getContent());
            pstmt.setString(3, homework.getDueDate().toString());
            if (homework.getLessonId() != null) {
                pstmt.setLong(4, homework.getLessonId());
            } else {
                pstmt.setNull(4, java.sql.Types.BIGINT);
            }
            pstmt.setLong(5, homework.getId());
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Failed to update homework: " + homework + ", error: " + e.getMessage());
            throw new RuntimeException("Failed to update homework", e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM homework WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Failed to delete homework: " + id + ", error: " + e.getMessage());
            throw new RuntimeException("Failed to delete homework", e);
        }
    }

    @Override
    public Optional<Homework> findById(Long id) {
        String sql = "SELECT * FROM homework WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToHomework(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to find homework by id: " + id + ", error: " + e.getMessage());
            throw new RuntimeException("Failed to find homework by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Homework> findAll() {
        String sql = "SELECT * FROM homework";
        List<Homework> homeworks = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                homeworks.add(mapResultSetToHomework(rs));
            }
        } catch (Exception e) {
            System.err.println("Failed to find all homeworks: " + e.getMessage());
            throw new RuntimeException("Failed to find all homeworks", e);
        }
        return homeworks;
    }

    private Homework mapResultSetToHomework(ResultSet rs) throws SQLException {
        Homework homework = new Homework();
        homework.setId(rs.getLong("id"));
        homework.setTitle(rs.getString("title"));
        homework.setContent(rs.getString("content"));
        homework.setDueDate(java.time.LocalDate.parse(rs.getString("due_date")));
        long lessonId = rs.getLong("lesson_id");
        if (!rs.wasNull()) {
            homework.setLessonId(lessonId);
        }
        return homework;
    }
}
