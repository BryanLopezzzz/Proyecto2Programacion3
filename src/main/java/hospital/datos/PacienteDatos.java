package hospital.datos;

import hospital.model.Paciente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PacienteDatos implements Plantilla {

    @Override
    public boolean insert(Object obj) throws SQLException {
        if (!(obj instanceof Paciente paciente)) return false;

        String sql = "INSERT INTO paciente (id, nombre, fecha_nacimiento, telefono) VALUES (?, ?, ?, ?)";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, paciente.getId());
            ps.setString(2, paciente.getNombre());
            ps.setDate(3, Date.valueOf(paciente.getFechaNacimiento()));
            ps.setString(4, paciente.getTelefono());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(Object obj) throws SQLException {
        if (!(obj instanceof Paciente paciente)) return false;

        String sql = "UPDATE paciente SET nombre = ?, fecha_nacimiento = ?, telefono = ? WHERE id = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, paciente.getNombre());
            ps.setDate(2, Date.valueOf(paciente.getFechaNacimiento()));
            ps.setString(3, paciente.getTelefono());
            ps.setString(4, paciente.getId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(String id) throws SQLException {
        String sql = "DELETE FROM paciente WHERE id = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean findById(int id) throws SQLException {
        String sql = "SELECT * FROM paciente WHERE id = ?";
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
        String sql = "SELECT * FROM paciente ORDER BY id";
        List<Object> lista = new ArrayList<>();

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Paciente p = new Paciente(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getDate("fecha_nacimiento").toLocalDate(),
                        rs.getString("telefono")
                );
                lista.add(p);
            }
        }
        return lista;
    }
}
