package lee.journalj.data.repository.implementation;

import lee.journalj.data.model.Lesson;
import java.util.ArrayList;
import java.util.List;

public class LessonRepositoryImplementation {
    private List<Lesson> lessons;

    public LessonRepositoryImplementation() {
        lessons = new ArrayList<>();
    }

    public void addLesson(Lesson lesson) {
        if (lesson != null) {
            lessons.add(lesson);
        }
    }

    public Lesson getLessonById(int id) {
        for (Lesson lesson : lessons) {
            if (lesson.getId() == id) {
                return lesson;
            }
        }
        return null;
    }

    public void printLessons() {
        for (Lesson lesson : lessons) {
            String name = lesson.getName();
            if (name != null) {
                System.out.println(name);
            } else {
                System.out.println("Lesson name is null");
            }
        }
    }
}