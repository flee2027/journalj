-- Таблица новостей
CREATE TABLE IF NOT EXISTS news (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    publication_date TEXT NOT NULL,
    author TEXT NOT NULL
);

-- Таблица домашних заданий
CREATE TABLE IF NOT EXISTS homework (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    due_date TEXT NOT NULL,
    lesson_id INTEGER REFERENCES lesson(id)
);

-- Таблица уроков
CREATE TABLE IF NOT EXISTS lesson (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject TEXT NOT NULL,
    room TEXT NOT NULL,
    day_of_week TEXT NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL,
    homework_id INTEGER REFERENCES homework(id),
    date DATE
);

-- Создание таблицы для учеников
CREATE TABLE IF NOT EXISTS students (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    middle_name TEXT,
    class TEXT
);

-- Создание таблицы для оценок
CREATE TABLE IF NOT EXISTS grades (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    subject TEXT NOT NULL,
    date DATE NOT NULL,
    value INTEGER NOT NULL,
    comment TEXT,
    FOREIGN KEY (student_id) REFERENCES students(id)
); 