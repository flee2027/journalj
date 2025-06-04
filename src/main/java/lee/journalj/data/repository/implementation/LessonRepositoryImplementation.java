package lee.journalj.data.repository.implementation;

import lee.journalj.data.model.Lesson;
import lee.journalj.data.repository.LessonRepository;
import lee.journalj.data.util.DatabaseHandler;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class LessonRepositoryImplementation implements LessonRepository {
    @Override
    public List<Lesson> findAll() {
        String sql = "SELECT * FROM lesson";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Lesson> lessons = new ArrayList<>();
                while (rs.next()) {
                    Integer homeworkId = null;
                    try {
                        homeworkId = rs.getObject("homework_id", Integer.class);
                    } catch (SQLException e) {
                        // Можно добавить логирование, если нужно
                    }
                    
                    // Получаем startTime и endTime как строки и преобразуем их в LocalTime
                    String startTimeStr = rs.getString("start_time");
                    String endTimeStr = rs.getString("end_time");
                    LocalTime startTime = startTimeStr != null ? LocalTime.parse(startTimeStr) : null;
                    LocalTime endTime = endTimeStr != null ? LocalTime.parse(endTimeStr) : null;
                    
                    Lesson lesson = new Lesson(
                            rs.getInt("id"),
                            rs.getString("subject"),
                            startTime,
                            endTime,
                            homeworkId
                    );
                    lesson.setDayOfWeek(rs.getString("day_of_week"));
                    
                    lessons.add(lesson);
                }
                return lessons;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка загрузки уроков: " + e.getMessage(), e);
        }
    }

    @Override
    public Lesson findById(int id) {
        String sql = "SELECT * FROM lesson WHERE id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Integer homeworkId = rs.getObject("homework_id", Integer.class);
                    Lesson lesson = new Lesson(
                            rs.getInt("id"),
                            rs.getString("subject"),
                            LocalTime.parse(rs.getString("start_time")),
                            LocalTime.parse(rs.getString("end_time")),
                            homeworkId != null ? homeworkId : null
                    );
                    lesson.setDayOfWeek(rs.getString("day_of_week"));
                    return lesson;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка загрузки урока: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void save(Lesson lesson) {
        String sql = "INSERT INTO lesson(subject, start_time, end_time, classroom, homework_id, day_of_week) VALUES(?,?,?,?,?,?)";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, lesson.getSubject());
            pstmt.setString(2, lesson.getStartTime().toString());
            pstmt.setString(3, lesson.getEndTime().toString());
            pstmt.setString(4, lesson.getClassroom());
            pstmt.setObject(5, lesson.getHomeworkId(), java.sql.Types.INTEGER);
            pstmt.setString(6, lesson.getDayOfWeek()); // Сохраняем день недели
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения урока: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(int id, Lesson lesson) {
        String sql = "UPDATE lesson SET subject = ?, start_time = ?, end_time = ?, classroom = ?, homework_id = ? WHERE id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, lesson.getSubject());
            pstmt.setTime(2, Time.valueOf(lesson.getStartTime()));
            pstmt.setTime(3, Time.valueOf(lesson.getEndTime()));
            pstmt.setString(4, lesson.getClassroom());
            pstmt.setObject(5, lesson.getHomeworkId());
            pstmt.setInt(6, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка обновления урока: " + e.getMessage(), e);
        }
    }



    @Override
    public void delete(int id) {
        String sql = "DELETE FROM lesson WHERE id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка удаления урока: " + e.getMessage(), e);
        }
    }
}
