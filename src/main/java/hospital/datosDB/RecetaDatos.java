package hospital.datosDB;

import hospital.model.EstadoReceta;
import hospital.model.Medico;
import hospital.model.Paciente;
import hospital.model.Receta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecetaDatos implements Plantilla {

    @Override
    public boolean insert(Object obj) throws SQLException {
        if (!(obj instanceof Receta receta)) return false;

        String sql = """
                INSERT INTO receta (id, paciente_id, medico_id, fecha, fecha_retiro, estado)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, receta.getId());
            ps.setString(2, receta.getPaciente().getId());
            ps.setString(3, receta.getMedico().getId());
            ps.setDate(4, Date.valueOf(receta.getFecha()));
            ps.setDate(5, Date.valueOf(receta.getFechaRetiro()));
            ps.setString(6, receta.getEstado().name());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(Object obj) throws SQLException {
        if (!(obj instanceof Receta receta)) return false;

        String sql = "UPDATE receta SET fecha_retiro = ?, estado = ? WHERE id = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(receta.getFechaRetiro()));
            ps.setString(2, receta.getEstado().name());
            ps.setString(3, receta.getId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM receta WHERE id = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(id));
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean findById(int id) throws SQLException {
        String sql = "SELECT id FROM receta WHERE id = ?";
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
        String sql = "SELECT * FROM receta ORDER BY id";
        List<Object> lista = new ArrayList<>();

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Receta receta = new Receta();
                receta.setId(rs.getString("id"));

                Paciente paciente = new Paciente();
                paciente.setId(rs.getString("paciente_id"));
                receta.setPaciente(paciente);

                Medico medico = new Medico();
                medico.setId(rs.getString("medico_id"));
                receta.setMedico(medico);

                receta.setFecha(rs.getDate("fecha").toLocalDate());
                receta.setFechaRetiro(rs.getDate("fecha_retiro").toLocalDate());
                receta.setEstado(EstadoReceta.valueOf(rs.getString("estado")));

                lista.add(receta);
            }
        }
        return lista;
    }

}
