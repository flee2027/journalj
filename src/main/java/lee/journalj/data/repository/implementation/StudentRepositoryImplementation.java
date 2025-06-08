package lee.journalj.data.repository.implementation;

import lee.journalj.data.model.Student;
import lee.journalj.data.repository.BaseRepository;
import lee.journalj.data.repository.StudentRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentRepositoryImplementation extends BaseRepository<Student> implements StudentRepository {
    @Override
    public List<Student> findAll() {
        String sql = "SELECT * FROM students";
        List<Student> students = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error finding all students", e);
        }
        return students;
    }

    @Override
    public Student save(Student student) {
        String sql = "INSERT INTO students(first_name, last_name, class) VALUES(?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, student.getFirstName());
            pstmt.setString(2, student.getLastName());
            pstmt.setString(3, student.getClassName());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    student.setId(generatedKeys.getLong(1));
                }
            }
            return student;
        } catch (Exception e) {
            throw new RuntimeException("Error saving student", e);
        }
    }

    @Override
    public Optional<Student> findById(Long id) {
        String sql = "SELECT * FROM students WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToStudent(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error finding student by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Student> findByClass(String className) {
        String sql = "SELECT * FROM students WHERE class = ?";
        List<Student> students = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, className);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error finding students by class", e);
        }
        return students;
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error deleting student", e);
        }
    }

    @Override
    public void update(Student student) {
        String sql = "UPDATE students SET first_name = ?, last_name = ?, class = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, student.getFirstName());
            pstmt.setString(2, student.getLastName());
            pstmt.setString(3, student.getClassName());
            pstmt.setLong(4, student.getId());
            pstmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error updating student", e);
        }
    }

    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setId(rs.getLong("id"));
        student.setFirstName(rs.getString("first_name"));
        student.setLastName(rs.getString("last_name"));
        student.setClassName(rs.getString("class"));
        return student;
    }
} 