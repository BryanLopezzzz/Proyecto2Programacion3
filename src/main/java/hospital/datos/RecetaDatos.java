package hospital.datos;

import hospital.model.*;

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
    public boolean delete(String id) throws SQLException {
        String sql = "DELETE FROM receta WHERE id = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, id);
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
        String sql = """
            SELECT 
                r.id,
                r.paciente_id,
                r.medico_id,
                r.fecha,
                r.fecha_retiro,
                r.estado,
                -- Datos del paciente
                p.nombre AS paciente_nombre,
                p.fecha_nacimiento AS paciente_fecha_nacimiento,
                p.telefono AS paciente_telefono,
                -- Datos del médico
                m.nombre AS medico_nombre,
                m.especialidad AS medico_especialidad
            FROM receta r
            INNER JOIN paciente p ON r.paciente_id = p.id
            INNER JOIN medico m ON r.medico_id = m.id
            ORDER BY r.fecha DESC
            """;

        List<Object> lista = new ArrayList<>();

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Receta receta = new Receta();
                receta.setId(rs.getString("id"));

                Paciente paciente = new Paciente();
                paciente.setId(rs.getString("paciente_id"));
                paciente.setNombre(rs.getString("paciente_nombre"));
                paciente.setFechaNacimiento(rs.getDate("paciente_fecha_nacimiento").toLocalDate());
                paciente.setTelefono(rs.getString("paciente_telefono"));
                receta.setPaciente(paciente);

                Medico medico = new Medico();
                medico.setId(rs.getString("medico_id"));
                medico.setNombre(rs.getString("medico_nombre"));
                medico.setEspecialidad(rs.getString("medico_especialidad"));
                receta.setMedico(medico);

                receta.setFecha(rs.getDate("fecha").toLocalDate());
                receta.setFechaRetiro(rs.getDate("fecha_retiro").toLocalDate());
                receta.setEstado(EstadoReceta.valueOf(rs.getString("estado")));

                try {
                    List<DetalleReceta> detalles = listarDetallesPorReceta(receta.getId());
                    receta.setDetalles(detalles);
                } catch (Exception e) {
                    receta.setDetalles(new ArrayList<>());
                }

                lista.add(receta);
            }
        }

        return lista;
    }

    public List<Receta> listarRecetasPorPaciente(String idPaciente) throws SQLException {
        List<Receta> recetas = new ArrayList<>();

        String sql = """
            SELECT 
                r.id,
                r.paciente_id,
                r.medico_id,
                r.fecha,
                r.fecha_retiro,
                r.estado,
                p.nombre AS paciente_nombre,
                p.fecha_nacimiento AS paciente_fecha_nacimiento,
                p.telefono AS paciente_telefono,
                m.nombre AS medico_nombre,
                m.especialidad AS medico_especialidad
            FROM receta r
            INNER JOIN paciente p ON r.paciente_id = p.id
            INNER JOIN medico m ON r.medico_id = m.id
            WHERE r.paciente_id = ?
            """;

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, idPaciente);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Receta receta = new Receta();
                    receta.setId(rs.getString("id"));
                    receta.setFecha(rs.getDate("fecha").toLocalDate());
                    receta.setEstado(EstadoReceta.valueOf(rs.getString("estado")));

                    Date fechaRetiroSql = rs.getDate("fecha_retiro");
                    if (fechaRetiroSql != null) {
                        receta.setFechaRetiro(fechaRetiroSql.toLocalDate());
                    }

                    Paciente paciente = new Paciente();
                    paciente.setId(rs.getString("paciente_id"));
                    paciente.setNombre(rs.getString("paciente_nombre"));
                    paciente.setFechaNacimiento(rs.getDate("paciente_fecha_nacimiento").toLocalDate());
                    paciente.setTelefono(rs.getString("paciente_telefono"));
                    receta.setPaciente(paciente);

                    Medico medico = new Medico();
                    medico.setId(rs.getString("medico_id"));
                    medico.setNombre(rs.getString("medico_nombre"));
                    medico.setEspecialidad(rs.getString("medico_especialidad"));
                    receta.setMedico(medico);

                    List<DetalleReceta> detalles = listarDetallesPorReceta(receta.getId());
                    receta.setDetalles(detalles);

                    recetas.add(receta);
                }
            }
        }

        return recetas;
    }
    private List<DetalleReceta> listarDetallesPorReceta(String idReceta) throws SQLException {
        List<DetalleReceta> detalles = new ArrayList<>();

        String sql = "SELECT dr.*, m.nombre as med_nombre, m.presentacion as med_presentacion " +
                "FROM detalle_receta dr " +
                "INNER JOIN medicamento m ON dr.medicamento_codigo = m.codigo " +
                "WHERE dr.receta_id = ?";

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, idReceta);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleReceta detalle = new DetalleReceta();

                    Medicamento medicamento = new Medicamento();
                    medicamento.setCodigo(rs.getString("medicamento_codigo"));
                    medicamento.setNombre(rs.getString("med_nombre"));
                    medicamento.setPresentacion(rs.getString("med_presentacion"));

                    detalle.setMedicamento(medicamento);
                    detalle.setCantidad(rs.getInt("cantidad"));
                    detalle.setIndicaciones(rs.getString("indicaciones"));

                    int diasTratamiento = rs.getInt("dias_tratamiento");
                    if (rs.wasNull()) {
                        diasTratamiento = 7; //Un valor por defecto
                    }
                    detalle.setDiasTratamiento(diasTratamiento);

                    detalles.add(detalle);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error cargando detalles de receta " + idReceta + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }

        return detalles;
    }

}
