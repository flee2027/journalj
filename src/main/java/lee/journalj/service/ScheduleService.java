package lee.journalj.service;

import lee.journalj.data.model.Homework;
import lee.journalj.data.model.Lesson;
import lee.journalj.data.repository.HomeworkRepository;
import lee.journalj.data.repository.LessonRepository;

import java.util.List;

public class ScheduleService {
    // Исправлено: зависимость от интерфейсов, а не от реализации
    private final LessonRepository lessonRepo;
    private final HomeworkRepository homeworkRepo;

    public ScheduleService(LessonRepository lessonRepo, HomeworkRepository homeworkRepo) {
        this.lessonRepo = lessonRepo;
        this.homeworkRepo = homeworkRepo;
    }

    public List<Lesson> getAllLessons() {
        return lessonRepo.findAll();
    }

    public void saveLesson(Lesson lesson) {
        lessonRepo.save(lesson);
    }

    // Исправлен метод update с корректной сигнатурой
    public void updateLesson(Lesson lesson) {
        lessonRepo.update(lesson.getId(), lesson);
    }
    public void deleteLesson(int id) {
        lessonRepo.delete(id);
    }

    public void saveHomework(Homework homework) {
        homeworkRepo.save(homework);
    }

    public Homework getHomeworkById(int id) {
        return homeworkRepo.findById(id).orElseGet(() -> {
            Homework emptyHomework = new Homework();
            emptyHomework.setContent(""); // Инициализация пустого содержимого
            return emptyHomework;
        });
    }
}