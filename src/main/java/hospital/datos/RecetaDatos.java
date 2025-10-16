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
        String sql = "SELECT * FROM receta ORDER BY fecha DESC";
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

                try {
                    List<DetalleReceta> detalles = listarDetallesPorReceta(receta.getId());
                    receta.setDetalles(detalles);
                    System.out.println("✓ Receta " + receta.getId() + " cargada con " + detalles.size() + " detalles");
                } catch (Exception e) {
                    System.err.println("✗ Error cargando detalles de receta " + receta.getId());
                    receta.setDetalles(new ArrayList<>());
                }

                lista.add(receta);
            }
        }

        System.out.println("Total de recetas cargadas: " + lista.size());
        return lista;
    }

    public List<Receta> listarRecetasPorPaciente(String idPaciente) throws SQLException {
        List<Receta> recetas = new ArrayList<>();

        String sql = "SELECT * FROM receta WHERE idPaciente = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, idPaciente);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Receta receta = new Receta();

                    receta.setId(rs.getString("id"));
                    receta.setFecha(rs.getDate("fecha").toLocalDate());
                    receta.setEstado(EstadoReceta.valueOf(rs.getString("estado")));

                    Date fechaRetiroSql = rs.getDate("fechaRetiro");
                    if (fechaRetiroSql != null) {
                        receta.setFechaRetiro(fechaRetiroSql.toLocalDate());
                    }

                    Paciente paciente = new Paciente();
                    paciente.setId(rs.getString("idPaciente"));
                    receta.setPaciente(paciente);

                    Medico medico = new Medico();
                    medico.setId(rs.getString("idMedico"));
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
