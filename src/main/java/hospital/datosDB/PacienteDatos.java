package hospital.datosDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import hospital.model.Paciente;


public class PacienteDatos {
    public List<Paciente> findAll() throws SQLException {
        String sql = "SELECT * FROM paciente ORDER BY id";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Paciente> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(new Paciente(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getDate("fecha_nacimiento").toLocalDate(),
                        rs.getString("telefono")
                ));
            }
            return lista;
        }
    }

    public Paciente findById(String id) throws SQLException {
        String sql = "SELECT * FROM paciente WHERE id = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Paciente(
                            rs.getString("id"),
                            rs.getString("nombre"),
                            rs.getDate("fecha_nacimiento").toLocalDate(),
                            rs.getString("telefono")
                    );
                }
                return null;
            }
        }
    }

    public Paciente insert(Paciente paciente) throws SQLException {
        String sql = "INSERT INTO paciente (id, nombre, fecha_nacimiento, telefono) VALUES (?, ?, ?, ?)";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, paciente.getId());
            ps.setString(2, paciente.getNombre());
            ps.setDate(3, Date.valueOf(paciente.getFechaNacimiento()));
            ps.setString(4, paciente.getTelefono());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                return paciente;
            }
            return null;
        }
    }

    public Paciente update(Paciente paciente) throws SQLException {
        String sql = "UPDATE paciente SET nombre = ?, fecha_nacimiento = ?, telefono = ? WHERE id = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, paciente.getNombre());
            ps.setDate(2, Date.valueOf(paciente.getFechaNacimiento()));
            ps.setString(3, paciente.getTelefono());
            ps.setString(4, paciente.getId());

            if (ps.executeUpdate() > 0) {
                return paciente;
            }
            return null;
        }
    }

    public int delete(String id) throws SQLException {
        String sql = "DELETE FROM paciente WHERE id = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate();
        }
    }
}
/*
id VARCHAR(50) PRIMARY KEY,
nombre VARCHAR(100),
fecha_nacimiento DATE,
telefono VARCHAR(20)
 */