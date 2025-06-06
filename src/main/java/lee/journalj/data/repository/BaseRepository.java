package lee.journalj.data.repository;

import lee.journalj.data.util.DatabaseHandler;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public abstract class BaseRepository<T> implements Repository<T> {
    protected Connection getConnection() throws Exception {
        return DatabaseHandler.getInstance().getConnection();
    }
}