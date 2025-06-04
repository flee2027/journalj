package lee.journalj.service;

import lee.journalj.data.model.Homework;
import lee.journalj.data.repository.HomeworkRepository;
import java.util.List;
import java.util.Optional;

public class HomeworkService {
    private final HomeworkRepository homeworkRepo;

    public HomeworkService(HomeworkRepository homeworkRepo) {
        this.homeworkRepo = homeworkRepo;
    }

    public Optional<Homework> findById(int id) {
        return homeworkRepo.findById(id);
    }

    public void save(Homework homework) {
        homeworkRepo.save(homework);
    }

    public void update(Homework homework) {
        homeworkRepo.update(homework);
    }

    public List<Homework> getAllHomework() {
        return homeworkRepo.findAll();
    }
}