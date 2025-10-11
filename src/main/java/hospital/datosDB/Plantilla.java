package hospital.datosDB;

import java.sql.SQLException;
import java.util.List;

public interface Plantilla {
    public boolean insert(Object obj) throws SQLException;
    public boolean update(Object obj) throws SQLException;
    public boolean delete(int id) throws SQLException;
    public boolean findById(int id) throws SQLException;
    public List<Object> findAll() throws SQLException;
}
