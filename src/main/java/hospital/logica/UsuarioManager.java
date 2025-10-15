package hospital.logica;

import hospital.model.Usuario;
import hospital.model.Medico;
import hospital.model.Farmaceuta;
import hospital.model.Administrador;
import hospital.logica.MedicoLogica;
import hospital.logica.FarmaceutaLogica;
import hospital.logica.AdministradorLogica;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioManager {
    private final MedicoLogica medicoLogica = new MedicoLogica();
    private final FarmaceutaLogica farmaceutaLogica = new FarmaceutaLogica();
    private final AdministradorLogica administradorLogica = new AdministradorLogica();
    private static Usuario usuarioActual;

    public enum TipoUsuario {
        ADMINISTRADOR, MEDICO, FARMACEUTA, DESCONOCIDO
    }
    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static void logout() {
        usuarioActual = null;
    }


    // ==== LOGIN ====
    public Usuario login(String id, String clave) throws Exception {
        try {
            Usuario u = administradorLogica.buscarPorId(id);
            if (u != null && u.getClave().equals(clave)) return u;

            u = farmaceutaLogica.buscarPorId(id);
            if (u != null && u.getClave().equals(clave)) return u;

            u = medicoLogica.buscarPorId(id);
            if (u != null && u.getClave().equals(clave)) return u;

            throw new Exception("Credenciales incorrectas.");
        } catch (SQLException e) {
            throw new Exception("Error al buscar usuario: " + e.getMessage(), e);
        }
    }

    // ==== Cambio de clave ====
    public void cambiarClave(String id, String actual, String nueva) throws Exception {
        if (id == null || id.isBlank())
            throw new Exception("El id es obligatorio.");
        if (actual == null || actual.isBlank())
            throw new Exception("La clave actual es obligatoria.");
        if (nueva == null || nueva.isBlank())
            throw new Exception("La nueva clave no puede estar vacía.");

        // Buscar en cada tipo de usuario
        Administrador a = administradorLogica.buscarPorId(id);
        if (a != null) {
            if (!a.getClave().equals(actual))
                throw new Exception("Clave actual incorrecta.");
            a.setClave(nueva);
            administradorLogica.modificar(a);
            return;
        }

        Medico m = medicoLogica.buscarPorId(id);
        if (m != null) {
            if (!m.getClave().equals(actual))
                throw new Exception("Clave actual incorrecta.");
            m.setClave(nueva);
            medicoLogica.modificar(m); // Persistir
            return;
        }

        Farmaceuta f = farmaceutaLogica.buscarPorId(id);
        if (f != null) {
            if (!f.getClave().equals(actual))
                throw new Exception("Clave actual incorrecta.");
            f.setClave(nueva);
            farmaceutaLogica.modificar(f);
            return;
        }
        throw new Exception("Usuario no encontrado.");
    }

    public boolean existeUsuario(String id) throws SQLException {
        if (administradorLogica.buscarPorId(id) != null) return true;
        if (medicoLogica.buscarPorId(id) != null) return true;
        if (farmaceutaLogica.buscarPorId(id) != null) return true;
        return false;
    }

    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        usuarios.addAll(medicoLogica.listar());
        usuarios.addAll(farmaceutaLogica.listar());
        usuarios.addAll(administradorLogica.listar());
        return usuarios;
    }

    public TipoUsuario determinarTipoUsuario(String id) throws SQLException {
        if (id == null || id.isEmpty()) {
            return TipoUsuario.DESCONOCIDO;
        }

        if (administradorLogica.buscarPorId(id) != null) return TipoUsuario.ADMINISTRADOR;
        if (medicoLogica.buscarPorId(id) != null) return TipoUsuario.MEDICO;
        if (farmaceutaLogica.buscarPorId(id) != null) return TipoUsuario.FARMACEUTA;

        return TipoUsuario.DESCONOCIDO;
    }

    public TipoUsuario getTipoUsuarioActual() throws SQLException {
        Usuario u = Sesion.getUsuario();
        if (u == null) {
            return TipoUsuario.DESCONOCIDO;
        }
        return determinarTipoUsuario(u.getId());
    }

    public boolean tienePermiso(String id, String funcionalidad) throws SQLException {
        TipoUsuario tipo = determinarTipoUsuario(id);

        switch (tipo) {
            case ADMINISTRADOR:
                return true;
            case MEDICO:
                return funcionalidad.equals("PRESCRIBIR") ||
                        funcionalidad.equals("DASHBOARD") ||
                        funcionalidad.equals("HISTORICO") ||
                        funcionalidad.equals("CAMBIAR_CLAVE") ||
                        funcionalidad.equals("ACERCA_DE");

            case FARMACEUTA:
                return funcionalidad.equals("DESPACHO") ||
                        funcionalidad.equals("DASHBOARD") ||
                        funcionalidad.equals("HISTORICO") ||
                        funcionalidad.equals("CAMBIAR_CLAVE") ||
                        funcionalidad.equals("ACERCA_DE");

            default:
                return false;
        }
    }

    public boolean usuarioActualTienePermiso(String funcionalidad) throws SQLException {
        if (usuarioActual == null) {
            return false;
        }
        return tienePermiso(usuarioActual.getId(), funcionalidad);
    }

    public String getNombreTipoUsuario(String id) throws SQLException {
        TipoUsuario tipo = determinarTipoUsuario(id);
        switch (tipo) {
            case ADMINISTRADOR: return "Administrador";
            case MEDICO: return "Médico";
            case FARMACEUTA: return "Farmaceuta";
            default: return "Usuario Desconocido";
        }
    }

    public String getNombreTipoUsuarioActual() throws SQLException {
        if (usuarioActual == null) {
            return "Sin usuario";
        }
        return getNombreTipoUsuario(usuarioActual.getId());
    }

    public boolean validarFormatoId(String id) {
        return id != null && !id.isBlank();
    }

    public String getMensajeErrorFormatoId(String id) {
        if (id == null || id.isEmpty()) {
            return "El ID no puede estar vacío.";
        }
        return "Formato de ID correcto.";
    }
}
