package lee.journalj.data.repository;

import lee.journalj.data.model.Lesson;

import java.util.List;

public interface LessonRepository {
    List<Lesson> findAll();
    Lesson findById(int id); // 添加此行以确保可被重写
    void save(Lesson lesson);
    void update(int id, Lesson lesson);
    void delete(int id);
}
