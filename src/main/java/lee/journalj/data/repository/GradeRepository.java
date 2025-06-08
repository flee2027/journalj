package lee.journalj.data.repository;

import lee.journalj.data.model.Grade;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GradeRepository {
    Grade save(Grade grade);
    Optional<Grade> findById(Long id);
    List<Grade> findByStudentId(Long studentId);
    List<Grade> findBySubject(String subject);
    List<Grade> findByStudentIdAndSubject(Long studentId, String subject);
    List<Grade> findBySubjectAndDate(String subject, LocalDate date);
    void delete(Long id);
    void update(Grade grade);
} 