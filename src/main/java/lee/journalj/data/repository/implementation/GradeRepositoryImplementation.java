package lee.journalj.data.repository.implementation;

import lee.journalj.data.model.Grade;
import lee.journalj.data.repository.BaseRepository;
import lee.journalj.data.repository.GradeRepository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GradeRepositoryImplementation extends BaseRepository<Grade> implements GradeRepository {
    @Override
    public List<Grade> findAll() {
        String sql = "SELECT * FROM grades";
        List<Grade> grades = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                grades.add(mapResultSetToGrade(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error finding all grades", e);
        }
        return grades;
    }

    @Override
    public Grade save(Grade grade) {
        String sql = "INSERT INTO grades(student_id, subject, date, value, comment) VALUES(?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, grade.getStudentId());
            pstmt.setString(2, grade.getSubject());
            pstmt.setDate(3, Date.valueOf(grade.getDate()));
            pstmt.setInt(4, grade.getValue());
            pstmt.setString(5, grade.getComment());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    grade.setId(generatedKeys.getLong(1));
                }
            }
            return grade;
        } catch (Exception e) {
            throw new RuntimeException("Error saving grade", e);
        }
    }

    @Override
    public Optional<Grade> findById(Long id) {
        String sql = "SELECT * FROM grades WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToGrade(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error finding grade by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Grade> findByStudentId(Long studentId) {
        String sql = "SELECT * FROM grades WHERE student_id = ?";
        List<Grade> grades = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                grades.add(mapResultSetToGrade(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error finding grades by student id", e);
        }
        return grades;
    }

    @Override
    public List<Grade> findBySubject(String subject) {
        String sql = "SELECT * FROM grades WHERE subject = ?";
        List<Grade> grades = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, subject);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                grades.add(mapResultSetToGrade(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error finding grades by subject", e);
        }
        return grades;
    }

    @Override
    public List<Grade> findByStudentIdAndSubject(Long studentId, String subject) {
        String sql = "SELECT * FROM grades WHERE student_id = ? AND subject = ?";
        List<Grade> grades = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, studentId);
            pstmt.setString(2, subject);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                grades.add(mapResultSetToGrade(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error finding grades by student id and subject", e);
        }
        return grades;
    }

    @Override
    public List<Grade> findBySubjectAndDate(String subject, LocalDate date) {
        String sql = "SELECT * FROM grades WHERE subject = ? AND date = ?";
        List<Grade> grades = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, subject);
            pstmt.setDate(2, Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                grades.add(mapResultSetToGrade(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error finding grades by subject and date", e);
        }
        return grades;
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM grades WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error deleting grade", e);
        }
    }

    @Override
    public void update(Grade grade) {
        String sql = "UPDATE grades SET student_id = ?, subject = ?, date = ?, value = ?, comment = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, grade.getStudentId());
            pstmt.setString(2, grade.getSubject());
            pstmt.setDate(3, Date.valueOf(grade.getDate()));
            pstmt.setInt(4, grade.getValue());
            pstmt.setString(5, grade.getComment());
            pstmt.setLong(6, grade.getId());
            pstmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error updating grade", e);
        }
    }

    private Grade mapResultSetToGrade(ResultSet rs) throws SQLException {
        Grade grade = new Grade();
        grade.setId(rs.getLong("id"));
        grade.setStudentId(rs.getLong("student_id"));
        grade.setSubject(rs.getString("subject"));
        grade.setDate(rs.getDate("date").toLocalDate());
        grade.setValue(rs.getInt("value"));
        grade.setComment(rs.getString("comment"));
        return grade;
    }
} 