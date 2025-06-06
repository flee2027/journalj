package lee.journalj.data.repository.implementation;

import lee.journalj.data.model.Lesson;
import lee.journalj.data.repository.BaseRepository;
import lee.journalj.data.repository.LessonRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Реализация репозитория для работы с уроками.
 */
public class LessonRepositoryImplementation extends BaseRepository<Lesson> implements LessonRepository {

    /**
     * Найти урок по идентификатору.
     */
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
            throw new RuntimeException("Failed to find lesson by id: " + id, e);
        }
        return Optional.empty();
    }

    /**
     * Получить все уроки.
     */
    @Override
    public List<Lesson> findAll() {
        String sql = "SELECT * FROM lesson";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            List<Lesson> lessons = new ArrayList<>();
            while (rs.next()) {
                lessons.add(mapResultSetToLesson(rs));
            }
            return lessons;
        } catch (Exception e) {
            System.err.println("Failed to find all lessons: " + e.getMessage());
            throw new RuntimeException("Failed to find all lessons", e);
        }
    }

    /**
     * Сохранить новый урок.
     */
    @Override
    public Lesson save(Lesson lesson) {
        if (lesson.getSubject() == null || lesson.getSubject().trim().isEmpty()) {
            throw new IllegalArgumentException("Subject must not be null or empty");
        }
        if (lesson.getDayOfWeek() == null || lesson.getDayOfWeek().trim().isEmpty()) {
            throw new IllegalArgumentException("Day of week must not be null or empty");
        }
        if (lesson.getStartTime() == null) {
            throw new IllegalArgumentException("Start time must not be null");
        }
        if (lesson.getEndTime() == null) {
            throw new IllegalArgumentException("End time must not be null");
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
            if (lesson.getDate() != null) {
                pstmt.setString(7, lesson.getDate().toString());
            } else {
                pstmt.setNull(7, java.sql.Types.VARCHAR);
            }
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    lesson.setId(rs.getLong(1));
                }
            }
            return lesson;
        } catch (Exception e) {
            System.err.println("Failed to save lesson: " + lesson + ", error: " + e.getMessage());
            throw new RuntimeException("Failed to save lesson: " + lesson, e);
        }
    }

    /**
     * Обновить урок.
     */
    @Override
    public void update(Lesson lesson) {
        String sql = "UPDATE lesson SET subject = ?, day_of_week = ?, start_time = ?, end_time = ?, room = ?, homework_id = ?, date = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, lesson.getSubject());
            pstmt.setString(2, lesson.getDayOfWeek());
            pstmt.setString(3, lesson.getStartTime().toString());
            pstmt.setString(4, lesson.getEndTime().toString());
            pstmt.setString(5, lesson.getRoom());
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
            throw new RuntimeException("Failed to update lesson: " + lesson, e);
        }
    }

    /**
     * Удалить урок по идентификатору.
     */
    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM lesson WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Failed to delete lesson by id: " + id + ", error: " + e.getMessage());
            throw new RuntimeException("Failed to delete lesson by id: " + id, e);
        }
    }

    /**
     * Маппинг ResultSet в Lesson.
     */
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
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                lesson.setDate(java.time.LocalDate.parse(dateStr));
            } catch (Exception e) {
                System.err.println("Failed to parse date: " + dateStr);
            }
        }
        return lesson;
    }
}