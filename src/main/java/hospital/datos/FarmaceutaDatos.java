package hospital.datos;

import hospital.model.Farmaceuta;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FarmaceutaDatos implements Plantilla {

    @Override
    public boolean insert(Object obj) throws SQLException {
        if (!(obj instanceof Farmaceuta farmaceuta)) return false;

        String sql = "INSERT INTO farmaceuta (id, clave, nombre) VALUES (?, ?, ?)";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, farmaceuta.getId());
            ps.setString(2, farmaceuta.getClave());
            ps.setString(3, farmaceuta.getNombre());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(Object obj) throws SQLException {
        if (!(obj instanceof Farmaceuta farmaceuta)) return false;

        String sql = "UPDATE farmaceuta SET clave = ?, nombre = ? WHERE id = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, farmaceuta.getClave());
            ps.setString(2, farmaceuta.getNombre());
            ps.setString(3, farmaceuta.getId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(String id) throws SQLException {
        String sql = "DELETE FROM farmaceuta WHERE id = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean findById(int id) throws SQLException {
        String sql = "SELECT * FROM farmaceuta WHERE id = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, String.valueOf(id));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public List<Object> findAll() throws SQLException {
        String sql = "SELECT * FROM farmaceuta ORDER BY id";
        List<Object> lista = new ArrayList<>();

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Farmaceuta f = new Farmaceuta(
                        rs.getString("id"),
                        rs.getString("clave"),
                        rs.getString("nombre")
                );
                lista.add(f);
            }
        }
        return lista;
    }

    // --- Métodos auxiliares más específicos (opcionales y útiles) ---

    public Farmaceuta buscarPorId(String id) throws SQLException {
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
            }
        }
        return null;
    }

    public List<Farmaceuta> listarTodos() throws SQLException {
        String sql = "SELECT * FROM farmaceuta ORDER BY id";
        List<Farmaceuta> lista = new ArrayList<>();
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Farmaceuta(
                        rs.getString("id"),
                        rs.getString("clave"),
                        rs.getString("nombre")
                ));
            }
        }
        return lista;
    }
}
