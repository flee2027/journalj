package lee.journalj.data.repository.implementation;

import lee.journalj.data.model.Lesson;
import lee.journalj.data.repository.BaseRepository;
import lee.journalj.data.repository.LessonRepository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Реализация репозитория уроков.
 */
public class ScheduleRepositoryImplementation extends BaseRepository<Lesson> implements LessonRepository {

    @Override
    public Lesson save(Lesson lesson) {
        if (lesson.getSubject() == null || lesson.getSubject().trim().isEmpty()) {
            throw new IllegalArgumentException("Subject must not be null or empty");
        }
        String sql = "INSERT INTO lesson(subject, room, day_of_week, start_time, end_time, homework_id, date) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, lesson.getSubject());
            pstmt.setString(2, lesson.getRoom());
            pstmt.setString(3, lesson.getDayOfWeek());
            pstmt.setString(4, lesson.getStartTime().toString());
            pstmt.setString(5, lesson.getEndTime().toString());
            if (lesson.getHomeworkId() != null) {
                pstmt.setLong(6, lesson.getHomeworkId());
            } else {
                pstmt.setNull(6, java.sql.Types.BIGINT);
            }
            pstmt.setString(7, lesson.getDate() != null ? lesson.getDate().toString() : null);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    lesson.setId(rs.getLong(1));
                }
            }
            return lesson;
        } catch (Exception e) {
            System.err.println("Failed to save lesson: " + lesson + ", error: " + e.getMessage());
            throw new RuntimeException("Failed to save lesson", e);
        }
    }

    @Override
    public void update(Lesson lesson) {
        String sql = "UPDATE lesson SET subject = ?, room = ?, day_of_week = ?, start_time = ?, end_time = ?, homework_id = ?, date = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, lesson.getSubject());
            pstmt.setString(2, lesson.getRoom());
            pstmt.setString(3, lesson.getDayOfWeek());
            pstmt.setString(4, lesson.getStartTime().toString());
            pstmt.setString(5, lesson.getEndTime().toString());
            if (lesson.getHomeworkId() != null) {
                pstmt.setLong(6, lesson.getHomeworkId());
            } else {
                pstmt.setNull(6, java.sql.Types.BIGINT);
            }
            pstmt.setString(7, lesson.getDate() != null ? lesson.getDate().toString() : null);
            pstmt.setLong(8, lesson.getId());
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Failed to update lesson: " + lesson + ", error: " + e.getMessage());
            throw new RuntimeException("Failed to update lesson", e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM lesson WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Failed to delete lesson: " + id + ", error: " + e.getMessage());
            throw new RuntimeException("Failed to delete lesson", e);
        }
    }

    @Override
    public Optional<Lesson> findById(Long id) {
        String sql = "SELECT * FROM lesson WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToLesson(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to find lesson by id: " + id + ", error: " + e.getMessage());
            throw new RuntimeException("Failed to find lesson by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Lesson> findAll() {
        String sql = "SELECT * FROM lesson";
        List<Lesson> lessons = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lessons.add(mapResultSetToLesson(rs));
            }
        } catch (Exception e) {
            System.err.println("Failed to find all lessons: " + e.getMessage());
            throw new RuntimeException("Failed to find all lessons", e);
        }
        return lessons;
    }

    private Lesson mapResultSetToLesson(ResultSet rs) throws SQLException {
        Lesson lesson = new Lesson();
        lesson.setId(rs.getLong("id"));
        lesson.setSubject(rs.getString("subject"));
        lesson.setRoom(rs.getString("room"));
        lesson.setDayOfWeek(rs.getString("day_of_week"));
        lesson.setStartTime(java.time.LocalTime.parse(rs.getString("start_time")));
        lesson.setEndTime(java.time.LocalTime.parse(rs.getString("end_time")));
        long homeworkId = rs.getLong("homework_id");
        if (!rs.wasNull()) {
            lesson.setHomeworkId(homeworkId);
        }
        String dateStr = rs.getString("date");
        if (dateStr != null) {
            lesson.setDate(java.time.LocalDate.parse(dateStr));
        }
        return lesson;
    }
} 