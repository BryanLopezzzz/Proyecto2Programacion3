package hospital.logica;

import hospital.datos.FarmaceutaDatos;
import hospital.model.Administrador;
import hospital.model.Farmaceuta;

import java.sql.SQLException;
import java.util.List;

public class FarmaceutaLogica {
    private final FarmaceutaDatos datos;

    public FarmaceutaLogica() {
        this.datos = new FarmaceutaDatos();
    }


    public List<Farmaceuta> listar() throws SQLException {
        return datos.listarTodos();
    }

    public List<Farmaceuta> listar(Administrador admin) throws Exception {
        validarAdmin(admin);
        return listar();
    }

    public Farmaceuta buscarPorId(String id) throws SQLException {
        if (id == null || id.isBlank()) return null;
        return datos.buscarPorId(id);
    }

    public Farmaceuta buscarPorId(Administrador admin, String id) throws Exception {
        validarAdmin(admin);
        return buscarPorId(id);
    }

    public List<Farmaceuta> buscarPorNombre(String nombre) throws SQLException {
        if (nombre == null) nombre = "";
        String q = nombre.toLowerCase();
        return datos.listarTodos().stream()
                .filter(f -> f.getNombre() != null && f.getNombre().toLowerCase().contains(q))
                .toList();
    }

    public List<Farmaceuta> buscarPorNombre(Administrador admin, String nombre) throws Exception {
        validarAdmin(admin);
        return buscarPorNombre(nombre);
    }

    public List<Farmaceuta> generarReporte(Administrador admin) throws Exception {
        validarAdmin(admin);

        return listar();
    }



    public void agregar(Farmaceuta farmaceuta) throws Exception {
        validarAlta(farmaceuta);

        if (datos.buscarPorId(farmaceuta.getId()) != null) {
            throw new Exception("Ya existe un farmaceuta con id: " + farmaceuta.getId());
        }

        // Regla: clave = id al agregar
        farmaceuta.setClave(farmaceuta.getId());

        if (!datos.insert(farmaceuta)) {
            throw new Exception("No se pudo agregar el farmaceuta");
        }
    }

    public void agregar(Administrador admin, Farmaceuta f) throws Exception {
        validarAdmin(admin);
        agregar(f);
    }

    public Farmaceuta actualizar(Farmaceuta farmaceuta) throws Exception {
        validarModificacion(farmaceuta);

        if (datos.buscarPorId(farmaceuta.getId()) == null) {
            throw new Exception("No existe farmaceuta con id: " + farmaceuta.getId());
        }

        if (!datos.update(farmaceuta)) {
            throw new Exception("No se pudo actualizar el farmaceuta");
        }

        return farmaceuta;
    }

    public void modificar(Farmaceuta farmaceuta) throws Exception {
        actualizar(farmaceuta);
    }

    public void modificar(Administrador admin, Farmaceuta f) throws Exception {
        validarAdmin(admin);
        modificar(f);
    }

    public boolean eliminar(String id) throws Exception {
        if (id == null || id.isBlank()) {
            throw new Exception("El ID es obligatorio");
        }
        return datos.delete(id);
    }

    public void borrar(Administrador admin, String id) throws Exception {
        validarAdmin(admin);
        eliminar(id);
    }

    // --------- Validaciones ---------

    private void validarAlta(Farmaceuta f) throws Exception {
        if (f == null) throw new Exception("El farmaceuta no puede ser nulo.");
        if (f.getId() == null || f.getId().isBlank())
            throw new Exception("El id es obligatorio.");
        if (f.getNombre() == null || f.getNombre().isBlank())
            throw new Exception("El nombre es obligatorio.");
    }

    private void validarModificacion(Farmaceuta f) throws Exception {
        if (f == null) throw new Exception("El farmaceuta no puede ser nulo.");
        if (f.getId() == null || f.getId().isBlank())
            throw new Exception("El id es obligatorio.");
        if (f.getNombre() == null || f.getNombre().isBlank())
            throw new Exception("El nombre es obligatorio.");
    }

    private void validarAdmin(Administrador admin) throws Exception {
        if (admin == null) {
            throw new Exception("Solo los administradores pueden ejecutar esta acción.");
        }
    }
}