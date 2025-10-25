package hospital.datos;

import hospital.model.DetalleReceta;
import hospital.model.Medicamento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetalleRecetaDatos {

    public boolean insert(String recetaId, DetalleReceta detalle) throws SQLException {
        if (detalle == null || detalle.getMedicamento() == null) {
            return false;
        }

        String sql = """
            INSERT INTO detalle_receta 
            (receta_id, medicamento_codigo, cantidad, indicaciones, dias_tratamiento)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, recetaId);
            ps.setString(2, detalle.getMedicamento().getCodigo());
            ps.setInt(3, detalle.getCantidad());
            ps.setString(4, detalle.getIndicaciones());
            ps.setInt(5, detalle.getDiasTratamiento());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean insertBatch(String recetaId, List<DetalleReceta> detalles) throws SQLException {
        if (detalles == null || detalles.isEmpty()) {
            return false;
        }

        String sql = """
            INSERT INTO detalle_receta 
            (receta_id, medicamento_codigo, cantidad, indicaciones, dias_tratamiento)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            cn.setAutoCommit(false);

            try {
                for (DetalleReceta detalle : detalles) {
                    if (detalle.getMedicamento() == null) continue;

                    ps.setString(1, recetaId);
                    ps.setString(2, detalle.getMedicamento().getCodigo());
                    ps.setInt(3, detalle.getCantidad());
                    ps.setString(4, detalle.getIndicaciones());
                    ps.setInt(5, detalle.getDiasTratamiento());
                    ps.addBatch();
                }

                int[] results = ps.executeBatch();
                cn.commit();

                // Verificar que todos se insertaron
                for (int result : results) {
                    if (result <= 0) {
                        return false;
                    }
                }

                return true;

            } catch (SQLException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public boolean update(String recetaId, String medicamentoCodigo, DetalleReceta detalle) throws SQLException {
        if (detalle == null) return false;

        String sql = """
            UPDATE detalle_receta 
            SET cantidad = ?, indicaciones = ?, dias_tratamiento = ?
            WHERE receta_id = ? AND medicamento_codigo = ?
            """;

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, detalle.getCantidad());
            ps.setString(2, detalle.getIndicaciones());
            ps.setInt(3, detalle.getDiasTratamiento());
            ps.setString(4, recetaId);
            ps.setString(5, medicamentoCodigo);

            return ps.executeUpdate() > 0;
        }
    }


    public boolean deleteByRecetaId(String recetaId) throws SQLException {
        String sql = "DELETE FROM detalle_receta WHERE receta_id = ?";

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, recetaId);
            return ps.executeUpdate() >= 0; // >= 0 porque puede no haber detalles
        }
    }

    public boolean delete(String recetaId, String medicamentoCodigo) throws SQLException {
        String sql = "DELETE FROM detalle_receta WHERE receta_id = ? AND medicamento_codigo = ?";

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, recetaId);
            ps.setString(2, medicamentoCodigo);
            return ps.executeUpdate() > 0;
        }
    }

    public List<DetalleReceta> findByRecetaId(String recetaId) throws SQLException {
        List<DetalleReceta> detalles = new ArrayList<>();

        String sql = """
            SELECT 
                dr.medicamento_codigo,
                dr.cantidad,
                dr.indicaciones,
                dr.dias_tratamiento,
                m.nombre as med_nombre,
                m.presentacion as med_presentacion
            FROM detalle_receta dr
            INNER JOIN medicamento m ON dr.medicamento_codigo = m.codigo
            WHERE dr.receta_id = ?
            ORDER BY dr.medicamento_codigo
            """;

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, recetaId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Medicamento medicamento = new Medicamento();
                    medicamento.setCodigo(rs.getString("medicamento_codigo"));
                    medicamento.setNombre(rs.getString("med_nombre"));
                    medicamento.setPresentacion(rs.getString("med_presentacion"));

                    DetalleReceta detalle = new DetalleReceta();
                    detalle.setMedicamento(medicamento);
                    detalle.setCantidad(rs.getInt("cantidad"));
                    detalle.setIndicaciones(rs.getString("indicaciones"));
                    detalle.setDiasTratamiento(rs.getInt("dias_tratamiento"));

                    detalles.add(detalle);
                }
            }
        }

        return detalles;
    }


    public boolean exists(String recetaId, String medicamentoCodigo) throws SQLException {
        String sql = "SELECT 1 FROM detalle_receta WHERE receta_id = ? AND medicamento_codigo = ?";

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, recetaId);
            ps.setString(2, medicamentoCodigo);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int countByRecetaId(String recetaId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM detalle_receta WHERE receta_id = ?";

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, recetaId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }
}