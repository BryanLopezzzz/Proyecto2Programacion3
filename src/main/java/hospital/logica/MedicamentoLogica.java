package hospital.logica;

import hospital.datos.MedicamentoDatos;
import hospital.model.Administrador;
import hospital.model.Medicamento;

import java.sql.SQLException;
import java.util.List;

public class MedicamentoLogica {
    private final MedicamentoDatos datos;

    public MedicamentoLogica() {
        this.datos = new MedicamentoDatos();
    }


    public List<Medicamento> listar() throws SQLException {
        List<Object> objetos = datos.findAll();
        return objetos.stream()
                .filter(obj -> obj instanceof Medicamento)
                .map(obj -> (Medicamento) obj)
                .toList();
    }

    public List<Medicamento> listar(Administrador admin) throws Exception {
        validarAdmin(admin);
        return listar();
    }

    public Medicamento buscarPorCodigo(String codigo) throws SQLException {
        if (codigo == null || codigo.isBlank()) return null;

        List<Medicamento> todos = listar();
        return todos.stream()
                .filter(m -> m.getCodigo().equalsIgnoreCase(codigo))
                .findFirst()
                .orElse(null);
    }

    public Medicamento buscarPorCodigo(Administrador admin, String codigo) throws Exception {
        validarAdmin(admin);
        return buscarPorCodigo(codigo);
    }

    public List<Medicamento> buscarPorNombre(String nombre) throws SQLException {
        if (nombre == null) nombre = "";
        String q = nombre.toLowerCase();
        return listar().stream()
                .filter(m -> m.getNombre() != null && m.getNombre().toLowerCase().contains(q))
                .toList();
    }

    public List<Medicamento> buscarPorNombre(Administrador admin, String nombre) throws Exception {
        validarAdmin(admin);
        return buscarPorNombre(nombre);
    }


    public void agregar(Medicamento medicamento) throws Exception {
        validarMedicamento(medicamento);

        // Verificar unicidad
        if (buscarPorCodigo(medicamento.getCodigo()) != null) {
            throw new Exception("Ya existe un medicamento con código: " + medicamento.getCodigo());
        }

        if (!datos.insert(medicamento)) {
            throw new Exception("No se pudo agregar el medicamento");
        }
    }

    public void agregar(Administrador admin, Medicamento medicamento) throws Exception {
        validarAdmin(admin);
        agregar(medicamento);
    }

    public Medicamento actualizar(Medicamento medicamento) throws Exception {
        validarMedicamento(medicamento);

        if (buscarPorCodigo(medicamento.getCodigo()) == null) {
            throw new Exception("No existe medicamento con código: " + medicamento.getCodigo());
        }

        if (!datos.update(medicamento)) {
            throw new Exception("No se pudo actualizar el medicamento");
        }

        return medicamento;
    }

    public void modificar(Medicamento medicamento) throws Exception {
        actualizar(medicamento);
    }

    public void modificar(Administrador admin, Medicamento medicamento) throws Exception {
        validarAdmin(admin);
        modificar(medicamento);
    }

    public boolean eliminar(String codigo) throws Exception {
        if (codigo == null || codigo.isBlank()) {
            throw new Exception("El código es obligatorio");
        }

        try {
            return datos.delete(codigo);
        } catch (NumberFormatException e) {
            throw new Exception("Código de medicamento inválido");
        }
    }

    public void borrar(Administrador admin, String codigo) throws Exception {
        validarAdmin(admin);
        eliminar(codigo);
    }

    public List<Medicamento> generarReporte(Administrador admin) throws Exception {
        validarAdmin(admin);
        return listar();
    }

    // --------- Validaciones ---------

    private void validarMedicamento(Medicamento m) throws Exception {
        if (m == null) throw new Exception("El medicamento no puede ser nulo.");
        if (m.getCodigo() == null || m.getCodigo().isBlank())
            throw new Exception("El código es obligatorio.");
        if (m.getNombre() == null || m.getNombre().isBlank())
            throw new Exception("El nombre es obligatorio.");
        if (m.getPresentacion() == null || m.getPresentacion().isBlank())
            throw new Exception("La presentación es obligatoria.");
    }

    private void validarAdmin(Administrador admin) throws Exception {
        if (admin == null) {
            throw new Exception("Solo los administradores pueden ejecutar esta acción.");
        }
    }
}
