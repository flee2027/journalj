package lee.journalj.service;

import lee.journalj.data.model.Student;
import lee.journalj.data.repository.StudentRepository;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Сервис для работы со студентами.
 */
public class StudentService {
    private static final Logger logger = Logger.getLogger(StudentService.class.getName());
    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        if (studentRepository == null) {
            throw new IllegalArgumentException("StudentRepository cannot be null");
        }
        this.studentRepository = studentRepository;
    }

    /**
     * Сохранить студента.
     * @param student студент для сохранения
     * @return сохраненный студент
     * @throws IllegalArgumentException если student == null или невалидный
     */
    public Student saveStudent(Student student) {
        validateStudent(student);
        try {
            Student savedStudent = studentRepository.save(student);
            logger.info("Student saved successfully: " + savedStudent.getId());
            return savedStudent;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to save student", e);
            throw new RuntimeException("Failed to save student", e);
        }
    }

    /**
     * Найти студента по id.
     * @param id идентификатор студента
     * @return Optional со студентом или пустой Optional, если студент не найден
     * @throws IllegalArgumentException если id == null
     */
    public Optional<Student> findStudentById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        try {
            return studentRepository.findById(id);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to find student by ID: " + id, e);
            throw new RuntimeException("Failed to find student", e);
        }
    }

    /**
     * Получить всех студентов.
     * @return список всех студентов
     */
    public List<Student> findAllStudents() {
        try {
            return studentRepository.findAll();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to get all students", e);
            throw new RuntimeException("Failed to get all students", e);
        }
    }

    /**
     * Удалить студента по id.
     * @param id идентификатор студента
     * @throws IllegalArgumentException если id == null
     */
    public void deleteStudent(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        try {
            studentRepository.delete(id);
            logger.info("Student deleted successfully: " + id);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to delete student: " + id, e);
            throw new RuntimeException("Failed to delete student", e);
        }
    }

    /**
     * Обновить студента.
     * @param student студент для обновления
     * @throws IllegalArgumentException если student == null или невалидный
     */
    public void updateStudent(Student student) {
        validateStudent(student);
        try {
            studentRepository.update(student);
            logger.info("Student updated successfully: " + student.getId());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to update student", e);
            throw new RuntimeException("Failed to update student", e);
        }
    }

    /**
     * Валидация студента.
     * @param student студент для валидации
     * @throws IllegalArgumentException если student невалидный
     */
    private void validateStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null");
        }
        if (student.getFirstName() == null || student.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("Student first name cannot be empty");
        }
        if (student.getLastName() == null || student.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Student last name cannot be empty");
        }
    }
} 