package lee.journalj.data.repository;

import lee.journalj.data.model.Homework;

import java.util.List;
import java.util.Optional;

public interface HomeworkRepository {
    Optional<Homework> findById(int id);
    List<Homework> findAll();
    Optional<Homework> save(Homework homework);
    void update(Homework homework);
    void delete(int id);
}
