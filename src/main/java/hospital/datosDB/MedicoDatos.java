package hospital.datosDB;

import hospital.model.Medico;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicoDatos implements Plantilla {

    @Override
    public boolean insert(Object obj) throws SQLException {
        if (!(obj instanceof Medico medico)) return false;

        String sql = "INSERT INTO medico (id, clave, nombre, especialidad) VALUES (?, ?, ?, ?)";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, medico.getId());
            ps.setString(2, medico.getClave());
            ps.setString(3, medico.getNombre());
            ps.setString(4, medico.getEspecialidad());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(Object obj) throws SQLException {
        if (!(obj instanceof Medico medico)) return false;

        String sql = "UPDATE medico SET clave = ?, nombre = ?, especialidad = ? WHERE id = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, medico.getClave());
            ps.setString(2, medico.getNombre());
            ps.setString(3, medico.getEspecialidad());
            ps.setString(4, medico.getId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM medico WHERE id = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, String.valueOf(id));
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean findById(int id) throws SQLException {
        String sql = "SELECT * FROM medico WHERE id = ?";
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
        String sql = "SELECT * FROM medico ORDER BY id";
        List<Object> lista = new ArrayList<>();

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Medico m = new Medico(
                        rs.getString("id"),
                        rs.getString("clave"),
                        rs.getString("nombre"),
                        rs.getString("especialidad")
                );
                lista.add(m);
            }
        }
        return lista;
    }

    // --- Métodos adicionales más prácticos (fuera de la interfaz) ---

    public Medico buscarPorId(String id) throws SQLException {
        String sql = "SELECT * FROM medico WHERE id = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Medico(
                            rs.getString("id"),
                            rs.getString("clave"),
                            rs.getString("nombre"),
                            rs.getString("especialidad")
                    );
                }
            }
        }
        return null;
    }

    public List<Medico> listarTodos() throws SQLException {
        String sql = "SELECT * FROM medico ORDER BY id";
        List<Medico> lista = new ArrayList<>();

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Medico(
                        rs.getString("id"),
                        rs.getString("clave"),
                        rs.getString("nombre"),
                        rs.getString("especialidad")
                ));
            }
        }
        return lista;
    }
}
