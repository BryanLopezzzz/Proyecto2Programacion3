package hospital.logica;

import hospital.datos.MedicoDatos;
import hospital.model.Administrador;
import hospital.model.Medico;

import java.sql.SQLException;
import java.util.List;

public class MedicoLogica {
    private final MedicoDatos datos;

    public MedicoLogica() {
        this.datos = new MedicoDatos();
    }


    public List<Medico> listar() throws SQLException {
        return datos.listarTodos();
    }

    public List<Medico> listar(Administrador admin) throws Exception {
        validarAdmin(admin);
        return listar();
    }

    public Medico buscarPorId(String id) throws SQLException {
        if (id == null || id.isBlank()) return null;
        return datos.buscarPorId(id);
    }

    public Medico buscarPorId(Administrador admin, String id) throws Exception {
        validarAdmin(admin);
        return buscarPorId(id);
    }

    public List<Medico> buscarPorNombre(String nombre) throws SQLException {
        if (nombre == null) nombre = "";
        String q = nombre.toLowerCase();
        return datos.listarTodos().stream()
                .filter(m -> m.getNombre() != null && m.getNombre().toLowerCase().contains(q))
                .toList();
    }

    public List<Medico> buscarPorNombre(Administrador admin, String nombre) throws Exception {
        validarAdmin(admin);
        return buscarPorNombre(nombre);
    }

    public List<Medico> generarReporte(Administrador admin) throws Exception {
        validarAdmin(admin);
        return listar();
    }


    public void agregar(Medico medico) throws Exception {
        validarMedicoAlta(medico);

        // Verificar unicidad
        if (datos.buscarPorId(medico.getId()) != null) {
            throw new Exception("Ya existe un médico con el id: " + medico.getId());
        }

        // Regla: clave inicial = id
        medico.setClave(medico.getId());

        if (!datos.insert(medico)) {
            throw new Exception("No se pudo agregar el médico");
        }
    }

    public void agregar(Administrador admin, Medico medico) throws Exception {
        validarAdmin(admin);
        agregar(medico);
    }

    public Medico actualizar(Medico medico) throws Exception {
        validarMedicoModificacion(medico);

        if (datos.buscarPorId(medico.getId()) == null) {
            throw new Exception("No existe médico con id: " + medico.getId());
        }

        if (!datos.update(medico)) {
            throw new Exception("No se pudo actualizar el médico");
        }

        return medico;
    }

    public void modificar(Medico medico) throws Exception {
        actualizar(medico);
    }

    public void modificar(Administrador admin, Medico medico) throws Exception {
        validarAdmin(admin);
        modificar(medico);
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

    private void validarMedicoAlta(Medico m) throws Exception {
        if (m == null) throw new Exception("El médico no puede ser nulo.");
        if (m.getId() == null || m.getId().isBlank())
            throw new Exception("El id es obligatorio.");
        if (m.getNombre() == null || m.getNombre().isBlank())
            throw new Exception("El nombre es obligatorio.");
        if (m.getEspecialidad() == null || m.getEspecialidad().isBlank())
            throw new Exception("La especialidad es obligatoria.");
    }

    private void validarMedicoModificacion(Medico m) throws Exception {
        if (m == null) throw new Exception("El médico no puede ser nulo.");
        if (m.getId() == null || m.getId().isBlank())
            throw new Exception("El id es obligatorio.");
        if (m.getNombre() == null || m.getNombre().isBlank())
            throw new Exception("El nombre es obligatorio.");
        if (m.getEspecialidad() == null || m.getEspecialidad().isBlank())
            throw new Exception("La especialidad es obligatoria.");
    }

    private void validarAdmin(Administrador admin) throws Exception {
        if (admin == null) {
            throw new Exception("Solo los administradores pueden ejecutar esta acción.");
        }
    }
}