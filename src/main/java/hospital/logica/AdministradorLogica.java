package hospital.logica;

import hospital.datos.AdministradorDatos;
import hospital.model.Administrador;

import java.sql.SQLException;
import java.util.List;

public class AdministradorLogica {
    private final AdministradorDatos datos;

    public AdministradorLogica() {
        this.datos = new AdministradorDatos();
    }

    // --------- Lectura ---------

    public List<Administrador> listar() {
        try {
            return datos.listarTodos();
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar administradores: " + e.getMessage(), e);
        }
    }

    public Administrador buscarPorId(String id) {
        if (id == null || id.isBlank()) return null;
        try {
            return datos.buscarPorId(id);
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar administrador: " + e.getMessage(), e);
        }
    }

    public List<Administrador> buscarPorNombre(String nombre) {
        if (nombre == null) nombre = "";
        try {
            return datos.buscarPorNombre(nombre);
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar por nombre: " + e.getMessage(), e);
        }
    }

    // --------- Escritura ---------

    public void agregar(Administrador admin) throws Exception {
        validarAlta(admin);

        try {
            // Verificar unicidad de ID
            if (datos.buscarPorId(admin.getId()) != null) {
                throw new Exception("Ya existe un administrador con id: " + admin.getId());
            }

            // Regla: clave inicial = id
            admin.setClave(admin.getId());

            if (!datos.insert(admin)) {
                throw new Exception("No se pudo agregar el administrador");
            }
        } catch (SQLException e) {
            throw new Exception("Error al agregar administrador: " + e.getMessage(), e);
        }
    }

    public Administrador actualizar(Administrador admin) throws Exception {
        validarModificacion(admin);

        try {
            // Verificar que existe
            if (datos.buscarPorId(admin.getId()) == null) {
                throw new Exception("No existe administrador con id: " + admin.getId());
            }

            if (!datos.update(admin)) {
                throw new Exception("No se pudo actualizar el administrador");
            }

            return admin;
        } catch (SQLException e) {
            throw new Exception("Error al actualizar administrador: " + e.getMessage(), e);
        }
    }

    public void modificar(Administrador admin) throws Exception {
        actualizar(admin);
    }

    public boolean eliminar(String id) throws Exception {
        if (id == null || id.isBlank()) {
            throw new Exception("El ID es obligatorio");
        }
        try {
            return datos.delete(id);
        } catch (SQLException e) {
            throw new Exception("Error al eliminar administrador: " + e.getMessage(), e);
        }
    }

    // --------- Validaciones ---------

    private void validarAlta(Administrador a) throws Exception {
        if (a == null) throw new Exception("El administrador no puede ser nulo.");
        if (a.getId() == null || a.getId().isBlank())
            throw new Exception("El id es obligatorio.");
        if (a.getNombre() == null || a.getNombre().isBlank())
            throw new Exception("El nombre es obligatorio.");
    }

    private void validarModificacion(Administrador a) throws Exception {
        if (a == null) throw new Exception("El administrador no puede ser nulo.");
        if (a.getId() == null || a.getId().isBlank())
            throw new Exception("El id es obligatorio.");
        if (a.getNombre() == null || a.getNombre().isBlank())
            throw new Exception("El nombre es obligatorio.");
    }
}