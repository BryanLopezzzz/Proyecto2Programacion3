package hospital.logica;

import hospital.datos.PacienteDatos;
import hospital.model.Administrador;
import hospital.model.Paciente;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class PacienteLogica {
    private final PacienteDatos datos;

    public PacienteLogica() {
        this.datos = new PacienteDatos();
    }


    public List<Paciente> listar() throws SQLException {
        List<Object> objetos = datos.findAll();
        return objetos.stream()
                .filter(obj -> obj instanceof Paciente)
                .map(obj -> (Paciente) obj)
                .toList();
    }

    public List<Paciente> listar(Administrador admin) throws Exception {
        validarAdmin(admin);
        return listar();
    }

    public Paciente buscarPorId(String id) throws SQLException {
        if (id == null || id.isBlank()) return null;

        List<Paciente> todos = listar();
        return todos.stream()
                .filter(p -> p.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    public List<Paciente> buscarPorNombre(String nombre) throws SQLException {
        if (nombre == null) nombre = "";
        String q = nombre.toLowerCase();
        return listar().stream()
                .filter(p -> p.getNombre() != null && p.getNombre().toLowerCase().contains(q))
                .toList();
    }

    public List<Paciente> buscarPorNombre(Administrador admin, String nombre) throws Exception {
        validarAdmin(admin);
        return buscarPorNombre(nombre);
    }

    public void agregar(Paciente paciente) throws Exception {
        validarPaciente(paciente);

        // Verificar unicidad
        if (buscarPorId(paciente.getId()) != null) {
            throw new Exception("Ya existe un paciente con id: " + paciente.getId());
        }

        if (!datos.insert(paciente)) {
            throw new Exception("No se pudo agregar el paciente");
        }
    }

    public void agregar(Administrador admin, Paciente paciente) throws Exception {
        validarAdmin(admin);
        agregar(paciente);
    }

    public Paciente actualizar(Paciente paciente) throws Exception {
        validarPaciente(paciente);

        if (buscarPorId(paciente.getId()) == null) {
            throw new Exception("No existe paciente con id: " + paciente.getId());
        }

        if (!datos.update(paciente)) {
            throw new Exception("No se pudo actualizar el paciente");
        }

        return paciente;
    }

    public void modificar(Administrador admin, Paciente paciente) throws Exception {
        validarAdmin(admin);
        actualizar(paciente);
    }

    public boolean eliminar(String id) throws Exception {
        if (id == null || id.isBlank()) {
            throw new Exception("El ID es obligatorio");
        }

        try {
            return datos.delete(id);
        } catch (NumberFormatException e) {
            throw new Exception("ID de paciente inválido");
        }
    }

    public boolean eliminar(Administrador admin, String id) throws Exception {
        validarAdmin(admin);
        return eliminar(id);
    }

    public List<Paciente> generarReporte(Administrador admin) throws Exception {
        validarAdmin(admin);
        return listar();
    }
    // --------- Validaciones ---------

    private void validarPaciente(Paciente p) throws Exception {
        if (p == null) throw new Exception("El paciente no puede ser nulo.");
        if (p.getId() == null || p.getId().isBlank())
            throw new Exception("El ID es obligatorio.");
        if (p.getNombre() == null || p.getNombre().isBlank())
            throw new Exception("El nombre es obligatorio.");
        if (p.getFechaNacimiento() == null || p.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new Exception("La fecha de nacimiento no es válida.");
        }
        if (p.getTelefono() == null || p.getTelefono().isBlank()) {
            throw new Exception("El teléfono es obligatorio.");
        }
    }

    private void validarAdmin(Administrador admin) throws Exception {
        if (admin == null) {
            throw new Exception("Solo los administradores pueden ejecutar esta acción.");
        }
    }
}