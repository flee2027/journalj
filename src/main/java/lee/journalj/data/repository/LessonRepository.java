package lee.journalj.data.repository;

import lee.journalj.data.model.Lesson;

import java.util.List;

public interface LessonRepository {
    List<Lesson> findAll();
    void save(Lesson lesson);
    void update(Lesson lesson);
    void delete(int id);
}
