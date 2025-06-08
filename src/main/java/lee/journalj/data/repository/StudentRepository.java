package lee.journalj.data.repository;

import lee.journalj.data.model.Student;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends Repository<Student> {
    Student save(Student student);
    Optional<Student> findById(Long id);
    List<Student> findAll();
    void delete(Long id);
    void update(Student student);
    List<Student> findByClass(String className);
} 