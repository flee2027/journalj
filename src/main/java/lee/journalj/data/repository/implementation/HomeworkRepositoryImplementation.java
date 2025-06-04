package lee.journalj.data.repository.implementation;

import lee.journalj.data.model.Homework;
import lee.journalj.data.repository.HomeworkRepository;
import lee.journalj.data.util.DatabaseHandler;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HomeworkRepositoryImplementation implements HomeworkRepository {
    @Override
    public void update(Homework homework) {
        String sql = "UPDATE homework SET content = ? WHERE id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, homework.getContent());
            pstmt.setInt(2, homework.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update homework: " + homework, e);
        }
    }

    @Override
    public Optional<Homework> save(Homework homework) {
        String sql = "INSERT INTO homework(content) VALUES(?)";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, homework.getContent());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    homework.setId(rs.getInt(1));
                }
            }

            return Optional.of(homework);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save homework: " + homework, e);
        }
    }

    @Override
    public Optional<Homework> findById(int id) {
        String sql = "SELECT * FROM homework WHERE id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Homework(rs.getInt("id"), rs.getString("content"), null));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find homework by id: " + id, e);
        }
        return Optional.empty();
    }

    public List<Homework> findAll() {
        String sql = "SELECT * FROM homework";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Homework> homeworks = new ArrayList<>();
                while (rs.next()) {
                    homeworks.add(new Homework(rs.getInt("id"), rs.getString("content"), null));
                }
                return homeworks;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all homeworks", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM homework WHERE id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete homework by id: " + id, e);
        }
    }
}
