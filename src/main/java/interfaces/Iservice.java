package interfaces;

import java.sql.SQLException;
import java.util.List;

public interface Iservice <T> {

    public void create(T t) throws SQLException;

    public List<T> list()throws SQLException;

    public T read(int id)throws SQLException;


    public void update(T t)throws SQLException;
    public void delete(int id)throws SQLException;
}
