package hospital.datos;

import hospital.model.Administrador;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdministradorDatos implements Plantilla {

    // --------- Métodos de la interfaz Plantilla ---------

    @Override
    public boolean insert(Object obj) throws SQLException {
        if (!(obj instanceof Administrador admin)) return false;

        String sql = "INSERT INTO administrador (id, clave, nombre) VALUES (?, ?, ?)";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, admin.getId());
            ps.setString(2, admin.getClave());
            ps.setString(3, admin.getNombre());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(Object obj) throws SQLException {
        if (!(obj instanceof Administrador admin)) return false;

        String sql = "UPDATE administrador SET clave = ?, nombre = ? WHERE id = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, admin.getClave());
            ps.setString(2, admin.getNombre());
            ps.setString(3, admin.getId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(String id) throws SQLException {
        String sql = "DELETE FROM administrador WHERE id = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1,id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean findById(int id) throws SQLException {
        String sql = "SELECT * FROM administrador WHERE id = ?";
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
        String sql = "SELECT * FROM administrador ORDER BY nombre";
        List<Object> lista = new ArrayList<>();

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Administrador admin = new Administrador(
                        rs.getString("id"),
                        rs.getString("clave"),
                        rs.getString("nombre")
                );
                lista.add(admin);
            }
        }
        return lista;
    }

    // --------- Métodos auxiliares específicos (más prácticos) ---------

    /**
     * Busca un administrador por su ID (String)
     */
    public Administrador buscarPorId(String id) throws SQLException {
        String sql = "SELECT * FROM administrador WHERE id = ?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Administrador(
                            rs.getString("id"),
                            rs.getString("clave"),
                            rs.getString("nombre")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Lista todos los administradores ordenados por nombre
     */
    public List<Administrador> listarTodos() throws SQLException {
        String sql = "SELECT * FROM administrador ORDER BY nombre";
        List<Administrador> lista = new ArrayList<>();

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Administrador(
                        rs.getString("id"),
                        rs.getString("clave"),
                        rs.getString("nombre")
                ));
            }
        }
        return lista;
    }

    /**
     * Busca administradores por nombre (búsqueda parcial)
     */
    public List<Administrador> buscarPorNombre(String nombre) throws SQLException {
        String sql = "SELECT * FROM administrador WHERE nombre LIKE ? ORDER BY nombre";
        List<Administrador> lista = new ArrayList<>();

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, "%" + nombre + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Administrador(
                            rs.getString("id"),
                            rs.getString("clave"),
                            rs.getString("nombre")
                    ));
                }
            }
        }
        return lista;
    }
}
