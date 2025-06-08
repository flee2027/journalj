package lee.journalj.service;

import lee.journalj.data.model.Grade;
import lee.journalj.data.repository.GradeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

public class GradeService {
    private static final Logger logger = Logger.getLogger(GradeService.class.getName());
    private final GradeRepository gradeRepository;

    public GradeService(GradeRepository gradeRepository) {
        if (gradeRepository == null) {
            throw new IllegalArgumentException("GradeRepository cannot be null");
        }
        this.gradeRepository = gradeRepository;
    }

    public Grade saveGrade(Grade grade) {
        validateGrade(grade);
        try {
            return gradeRepository.save(grade);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to save grade", e);
            throw new RuntimeException("Failed to save grade", e);
        }
    }

    public Optional<Grade> findGradeById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Grade ID cannot be null");
        }
        try {
            return gradeRepository.findById(id);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to find grade by ID: " + id, e);
            throw new RuntimeException("Failed to find grade", e);
        }
    }

    public List<Grade> findGradesByStudentId(Long studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        try {
            return gradeRepository.findByStudentId(studentId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to find grades for student: " + studentId, e);
            throw new RuntimeException("Failed to find grades", e);
        }
    }

    public List<Grade> findGradesBySubject(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject cannot be null or empty");
        }
        try {
            return gradeRepository.findBySubject(subject);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to find grades for subject: " + subject, e);
            throw new RuntimeException("Failed to find grades", e);
        }
    }

    public List<Grade> findGradesByStudentIdAndSubject(Long studentId, String subject) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject cannot be null or empty");
        }
        try {
            return gradeRepository.findByStudentIdAndSubject(studentId, subject);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to find grades for student: " + studentId + " and subject: " + subject, e);
            throw new RuntimeException("Failed to find grades", e);
        }
    }

    public List<Grade> findGradesBySubjectAndDate(String subject, LocalDate date) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject cannot be null or empty");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        try {
            return gradeRepository.findBySubjectAndDate(subject, date);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to find grades for subject: " + subject + " and date: " + date, e);
            throw new RuntimeException("Failed to find grades", e);
        }
    }

    public void deleteGrade(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Grade ID cannot be null");
        }
        try {
            gradeRepository.delete(id);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to delete grade: " + id, e);
            throw new RuntimeException("Failed to delete grade", e);
        }
    }

    public void updateGrade(Grade grade) {
        validateGrade(grade);
        try {
            gradeRepository.update(grade);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to update grade: " + grade.getId(), e);
            throw new RuntimeException("Failed to update grade", e);
        }
    }

    public double calculateAverageGrade(Long studentId, String subject) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject cannot be null or empty");
        }
        try {
            List<Grade> grades = findGradesByStudentIdAndSubject(studentId, subject);
            if (grades.isEmpty()) {
                return 0.0;
            }
            return grades.stream()
                    .mapToInt(Grade::getValue)
                    .average()
                    .orElse(0.0);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to calculate average grade for student: " + studentId + " and subject: " + subject, e);
            throw new RuntimeException("Failed to calculate average grade", e);
        }
    }

    public List<LocalDate> getUniqueDatesForSubject(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject cannot be null or empty");
        }
        try {
            return gradeRepository.findBySubject(subject).stream()
                    .map(Grade::getDate)
                    .distinct()
                    .sorted()
                    .toList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to get unique dates for subject: " + subject, e);
            throw new RuntimeException("Failed to get unique dates", e);
        }
    }

    public Map<Long, Map<LocalDate, Grade>> getGradesBySubject(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject cannot be null or empty");
        }
        try {
            List<Grade> grades = gradeRepository.findBySubject(subject);
            Map<Long, Map<LocalDate, Grade>> result = new HashMap<>();
            
            for (Grade grade : grades) {
                result.computeIfAbsent(grade.getStudentId(), k -> new HashMap<>())
                      .put(grade.getDate(), grade);
            }
            
            return result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to get grades by subject: " + subject, e);
            throw new RuntimeException("Failed to get grades by subject", e);
        }
    }

    private void validateGrade(Grade grade) {
        if (grade == null) {
            throw new IllegalArgumentException("Grade cannot be null");
        }
        if (grade.getStudentId() == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        if (grade.getSubject() == null || grade.getSubject().trim().isEmpty()) {
            throw new IllegalArgumentException("Subject cannot be null or empty");
        }
        if (grade.getDate() == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (grade.getValue() < 0 || grade.getValue() > 100) {
            throw new IllegalArgumentException("Grade value must be between 0 and 100");
        }
    }
} 