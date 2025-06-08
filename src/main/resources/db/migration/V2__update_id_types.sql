-- Изменение типа id в таблице homework
ALTER TABLE homework RENAME TO homework_old;
CREATE TABLE homework (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    due_date TEXT NOT NULL,
    lesson_id INTEGER REFERENCES lesson(id)
);
INSERT INTO homework SELECT * FROM homework_old;
DROP TABLE homework_old;

-- Изменение типа id в таблице lesson
ALTER TABLE lesson RENAME TO lesson_old;
CREATE TABLE lesson (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject TEXT NOT NULL,
    room TEXT NOT NULL,
    day_of_week TEXT NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL,
    homework_id INTEGER REFERENCES homework(id),
    date DATE
);
INSERT INTO lesson SELECT * FROM lesson_old;
DROP TABLE lesson_old;

-- Изменение типа id в таблице students
ALTER TABLE students RENAME TO students_old;
CREATE TABLE students (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    middle_name TEXT,
    class TEXT
);
INSERT INTO students SELECT * FROM students_old;
DROP TABLE students_old;

-- Изменение типа id в таблице grades
ALTER TABLE grades RENAME TO grades_old;
CREATE TABLE grades (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    subject TEXT NOT NULL,
    date DATE NOT NULL,
    value INTEGER NOT NULL,
    comment TEXT,
    FOREIGN KEY (student_id) REFERENCES students(id)
);
INSERT INTO grades SELECT * FROM grades_old;
DROP TABLE grades_old;

-- Изменение типа id в таблице news
ALTER TABLE news RENAME TO news_old;
CREATE TABLE news (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    publication_date TEXT NOT NULL,
    author TEXT NOT NULL
);
INSERT INTO news SELECT * FROM news_old;
DROP TABLE news_old; 