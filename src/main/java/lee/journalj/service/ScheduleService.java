package lee.journalj.data.service;

import lee.journalj.data.model.Homework;
import lee.journalj.data.model.Lesson;
import lee.journalj.data.repository.HomeworkRepository;
import lee.journalj.data.repository.LessonRepository;

import java.util.List;
import java.util.Optional;

public class ScheduleService {
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

    public void updateLesson(Lesson lesson) {
        lessonRepo.update(lesson);
    }

    public void deleteLesson(int id) {
        lessonRepo.delete(id);
    }

    public void saveHomework(Homework homework) {
        homeworkRepo.save(homework);
    }

    public Optional<Homework> getHomeworkById(int id) {
        return homeworkRepo.findById(id);
    }
}
