package hospital.logica;

import hospital.datos.DetalleRecetaDatos;
import hospital.datos.RecetaDatos;
import hospital.model.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecetaLogica {
    private final RecetaDatos datos = new RecetaDatos();
    private final DetalleRecetaDatos detallesDatos = new DetalleRecetaDatos();

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
        if (receta.getDetalles() == null || receta.getDetalles().isEmpty()) {
            throw new Exception("La receta debe tener al menos un medicamento.");
        }

        try {
            boolean recetaInsertada = datos.insert(receta);
            if (!recetaInsertada) {
                throw new Exception("No se pudo insertar la receta en la base de datos.");
            }

            boolean detallesInsertados = detallesDatos.insertBatch(
                    receta.getId(),
                    receta.getDetalles()
            );

            if (!detallesInsertados) {
                try {
                    datos.delete(receta.getId());
                } catch (SQLException e) {
                    System.err.println("Error al limpiar receta fallida: " + e.getMessage());
                }
                throw new Exception("No se pudieron insertar los detalles de la receta.");
            }

            System.out.println("Receta creada exitosamente: " + receta.getId() +
                    " con " + receta.getDetalles().size() + " medicamentos");

            return receta;

        } catch (SQLException e) {
            throw new Exception("Error en base de datos al crear receta: " + e.getMessage(), e);
        }
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

    public Receta recargarRecetaCompleta(String recetaId) throws Exception {
        Receta receta = buscarPorId(recetaId);
        if (receta == null) {
            throw new Exception("Receta no encontrada: " + recetaId);
        }

        try {
            List<DetalleReceta> detalles = detallesDatos.findByRecetaId(recetaId);
            receta.setDetalles(detalles);
            return receta;
        } catch (SQLException e) {
            throw new Exception("Error al recargar detalles de receta: " + e.getMessage(), e);
        }
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
