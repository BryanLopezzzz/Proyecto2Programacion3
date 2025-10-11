package hospital.datosDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import hospital.model.Medico;

public class MedicoDatos {

    public List<Medico> findAll() throws SQLException {
        String sql = "SELECT * FROM medico ORDER BY id";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Medico> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(new Medico(
                        rs.getString("id"),
                        rs.getString("clave"),
                        rs.getString("nombre"),
                        rs.getString("especialidad")
                ));
            }
            return lista;
        }
    }

    public Medico findById(String id) throws SQLException {
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
                return null;
            }
        }
    }

    public Medico insert(Medico medico) throws SQLException {
        String sql = "INSERT INTO medico (id, clave, nombre, especialidad) VALUES (?, ?, ?, ?)";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, medico.getId());
            ps.setString(2, medico.getClave());
            ps.setString(3, medico.getNombre());
            ps.setString(4, medico.getEspecialidad());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                return medico;
            }
            return null;
        }
    }


    public Medico update(Medico medico) throws SQLException {
        String sql = "UPDATE medico SET clave = ?, nombre = ?, especialidad = ? WHERE id = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, medico.getClave());
            ps.setString(2, medico.getNombre());
            ps.setString(3, medico.getEspecialidad());
            ps.setString(4, medico.getId());

            if (ps.executeUpdate() > 0) {
                return medico;
            }
            return null;
        }
    }

    // Eliminar un médico por id
    public int delete(String id) throws SQLException {
        String sql = "DELETE FROM medico WHERE id = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate();
        }
    }
}

/*
CREATE TABLE medico (
    id VARCHAR(50) PRIMARY KEY,
    clave VARCHAR(100) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    especialidad VARCHAR(100) NOT NULL
);
 */
