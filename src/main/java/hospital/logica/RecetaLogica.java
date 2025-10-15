package hospital.logica;

import hospital.datos.RecetaDatos;
import hospital.model.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecetaLogica {
    private final RecetaDatos datos = new RecetaDatos();

    public List<Receta> listar() {
        try {
            List<Object> objetos = datos.findAll();
            List<Receta> recetas = new ArrayList<>();
            for (Object o : objetos) recetas.add((Receta) o);
            return recetas;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar recetas: " + e.getMessage(), e);
        }
    }

    public Receta buscarPorId(String id) {
        try {
            List<Receta> recetas = listar();
            for (Receta r : recetas) {
                if (r.getId().equalsIgnoreCase(id))
                    return r;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar receta: " + e.getMessage(), e);
        }
    }

    public Receta crearReceta(Receta receta) throws Exception {
        validarReceta(receta);
        if (datos.insert(receta))
            return receta;
        else
            throw new Exception("No se pudo insertar la receta en la base de datos.");
    }

    public Receta actualizar(Receta receta) throws Exception {
        validarReceta(receta);
        if (datos.update(receta))
            return receta;
        else
            throw new Exception("No se pudo actualizar la receta en la base de datos.");
    }

    public boolean eliminar(String id) throws Exception {
        try {
            return datos.delete(id);
        } catch (SQLException e) {
            throw new Exception("Error al eliminar receta: " + e.getMessage(), e);
        }
    }

    public void actualizarEstado(Farmaceuta farmaceuta, String idReceta, EstadoReceta nuevoEstado) throws Exception {
        if (farmaceuta == null)
            throw new Exception("Solo un farmaceuta puede actualizar el estado de una receta.");

        if (idReceta == null || idReceta.isBlank())
            throw new Exception("El ID de la receta es obligatorio.");

        Receta receta = buscarPorId(idReceta);
        if (receta == null)
            throw new Exception("No existe una receta con el ID: " + idReceta);

        receta.setEstado(nuevoEstado);

        if (!datos.update(receta))
            throw new Exception("No se pudo actualizar el estado de la receta.");
    }

    public List<Receta> listarRecetasPorPaciente(String idPaciente) throws SQLException {
        if (idPaciente == null || idPaciente.isBlank()) {
            throw new SQLException("El ID del paciente no puede estar vacío.");
        }

        return datos.listarRecetasPorPaciente(idPaciente);
    }

    // =========================
    // Validaciones
    // =========================
    private void validarReceta(Receta r) throws Exception {
        if (r == null) throw new Exception("La receta no puede ser nula.");
        if (r.getId() == null || r.getId().isBlank()) throw new Exception("El ID de la receta es obligatorio.");
        if (r.getPaciente() == null) throw new Exception("La receta debe tener un paciente asociado.");
        if (r.getMedico() == null) throw new Exception("La receta debe tener un médico asociado.");
        if (r.getFecha() == null) throw new Exception("La receta debe tener una fecha válida.");
        if (r.getFechaRetiro() == null) throw new Exception("La receta debe tener una fecha de retiro.");
    }
}
