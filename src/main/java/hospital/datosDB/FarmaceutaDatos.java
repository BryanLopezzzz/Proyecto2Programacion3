package hospital.datosDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import hospital.model.Farmaceuta;


public class FarmaceutaDatos {
    public List<Farmaceuta> findAll() throws SQLException {
        String sql = "SELECT * FROM farmaceuta ORDER BY id";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Farmaceuta> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(new Farmaceuta(
                        rs.getString("id"),
                        rs.getString("clave"),
                        rs.getString("nombre")
                ));
            }
            return lista;
        }
    }

    public Farmaceuta findById(String id) throws SQLException {
        String sql = "SELECT * FROM farmaceuta WHERE id = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Farmaceuta(
                            rs.getString("id"),
                            rs.getString("clave"),
                            rs.getString("nombre")
                    );
                }
                return null;
            }
        }
    }

    public Farmaceuta insert(Farmaceuta farmaceuta) throws SQLException {
        String sql = "INSERT INTO farmaceuta (id, clave, nombre) VALUES (?, ?, ?)";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, farmaceuta.getId());
            ps.setString(2, farmaceuta.getClave());
            ps.setString(3, farmaceuta.getNombre());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                return farmaceuta;
            }
            return null;
        }
    }

    public Farmaceuta update(Farmaceuta farmaceuta) throws SQLException {
        String sql = "UPDATE farmaceuta SET clave = ?, nombre = ? WHERE id = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, farmaceuta.getClave());
            ps.setString(2, farmaceuta.getNombre());
            ps.setString(3, farmaceuta.getId());

            if (ps.executeUpdate() > 0) {
                return farmaceuta;
            }
            return null;
        }
    }

    public int delete(String id) throws SQLException {
        String sql = "DELETE FROM farmaceuta WHERE id = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate();
        }
    }
}
/*
* CREATE TABLE farmaceuta (
    id VARCHAR(50) PRIMARY KEY,
    clave VARCHAR(100) NOT NULL,
    nombre VARCHAR(100) NOT NULL
);*/
