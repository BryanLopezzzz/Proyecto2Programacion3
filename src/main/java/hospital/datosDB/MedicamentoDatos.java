package hospital.datosDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import hospital.model.Medicamento;

public class MedicamentoDatos {

    public List<Medicamento> findAll() throws SQLException {
        String sql = "SELECT * FROM medicamento ORDER BY codigo";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Medicamento> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(new Medicamento(
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getString("presentacion")
                ));
            }
            return lista;
        }
    }

    public Medicamento findByCodigo(String codigo) throws SQLException {
        String sql = "SELECT * FROM medicamento WHERE codigo = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Medicamento(
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getString("presentacion")
                    );
                }
                return null;
            }
        }
    }

    public Medicamento insert(Medicamento medicamento) throws SQLException {
        String sql = "INSERT INTO medicamento (codigo, nombre, presentacion) VALUES (?, ?, ?)";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, medicamento.getCodigo());
            ps.setString(2, medicamento.getNombre());
            ps.setString(3, medicamento.getPresentacion());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                return medicamento;
            }
            return null;
        }
    }

    public Medicamento update(Medicamento medicamento) throws SQLException {
        String sql = "UPDATE medicamento SET nombre = ?, presentacion = ? WHERE codigo = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, medicamento.getNombre());
            ps.setString(2, medicamento.getPresentacion());
            ps.setString(3, medicamento.getCodigo());

            if (ps.executeUpdate() > 0) {
                return medicamento;
            }
            return null;
        }
    }

    public int delete(String codigo) throws SQLException {
        String sql = "DELETE FROM medicamento WHERE codigo = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            return ps.executeUpdate();
        }
    }
}

/*
* CREATE TABLE medicamento (
    codigo VARCHAR(50) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    presentacion VARCHAR(100) NOT NULL
);*/
