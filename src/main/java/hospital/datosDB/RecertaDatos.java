package hospital.datosDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import hospital.model.EstadoReceta;
import hospital.model.Medico;
import hospital.model.Paciente;
import hospital.model.Receta;

public class RecertaDatos {

    public List<Receta> findAll() throws SQLException {
        String sql = """
                SELECT r.id, r.paciente_id, r.medico_id, r.fecha, r.fecha_retiro, r.estado
                FROM receta r
                ORDER BY r.id
                """;
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Receta> lista = new ArrayList<>();
            while (rs.next()) {
                Receta receta = new Receta();
                receta.setId(rs.getString("id"));

                // Cargar referencias solo con ID (se puede expandir si tienes DAOs de Paciente/Medico)
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
            return lista;
        }
    }

    public Receta findById(String id) throws SQLException {
        String sql = """
                SELECT id, paciente_id, medico_id, fecha, fecha_retiro, estado
                FROM receta
                WHERE id = ?
                """;
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
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

                    return receta;
                }
                return null;
            }
        }
    }

    public Receta insert(Receta receta) throws SQLException {
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

            int filas = ps.executeUpdate();
            if (filas > 0) {
                return receta;
            }
            return null;
        }
    }

    public Receta update(Receta receta) throws SQLException {
        String sql = """
                UPDATE receta
                SET fecha_retiro = ?, estado = ?
                WHERE id = ?
                """;
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(receta.getFechaRetiro()));
            ps.setString(2, receta.getEstado().name());
            ps.setString(3, receta.getId());

            if (ps.executeUpdate() > 0) {
                return receta;
            }
            return null;
        }
    }

    public int delete(String id) throws SQLException {
        String sql = "DELETE FROM receta WHERE id = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate();
        }
    }
}

/*
CREATE TABLE receta (
    id VARCHAR(50) PRIMARY KEY,
    paciente_id VARCHAR(50) NOT NULL,
    medico_id VARCHAR(50) NOT NULL,
    fecha DATE NOT NULL,
    fecha_retiro DATE NOT NULL,
    estado VARCHAR(20) NOT NULL,
    FOREIGN KEY (paciente_id) REFERENCES paciente(id),
    FOREIGN KEY (medico_id) REFERENCES medico(id)
);
*/
