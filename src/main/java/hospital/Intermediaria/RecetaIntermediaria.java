package hospital.Intermediaria;

import hospital.model.*;

import java.util.List;
import java.util.stream.Collectors;
import hospital.model.EstadoReceta;
import hospital.model.Farmaceuta;
import hospital.model.Medico;
import hospital.model.Receta;
import hospital.logica.RecetaLogica;

public class RecetaIntermediaria {
    private final RecetaLogica recetaLogica;

    public RecetaIntermediaria() {
        this.recetaLogica = new RecetaLogica();
    }

    // MÉDICOS
    public Receta crearReceta(Medico medico, Receta receta) throws Exception {
        validarMedico(medico);
        return recetaLogica.crearReceta(receta);
    }

    public void agregarDetalle(Medico medico, String recetaId, String medicamentoId, int cantidad, String indicaciones,int diasTratamiento) throws Exception {
        validarMedico(medico);
        recetaLogica.agregarDetalle(recetaId, medicamentoId, cantidad, indicaciones,diasTratamiento);
    }

    // FARMACEUTAS
    public void actualizarEstado(Farmaceuta farmaceuta, String recetaId, EstadoReceta nuevoEstado) throws Exception {
        validarFarmaceuta(farmaceuta);
        recetaLogica.actualizarEstado(recetaId, nuevoEstado);
    }

    // Método para listar recetas de un paciente específico
    public List<Receta> listarRecetasPorPaciente(String pacienteId) throws Exception {
        if (pacienteId == null || pacienteId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del paciente no puede estar vacío");
        }

        try {
            // Obtener todas las recetas
            List<Receta> todasLasRecetas = listarRecetas();

            // Filtrar por paciente
            return todasLasRecetas.stream()
                    .filter(receta -> receta.getPaciente() != null &&
                            pacienteId.equals(receta.getPaciente().getId()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new Exception("Error al buscar recetas del paciente: " + e.getMessage(), e);
        }
    }

    // Método para buscar recetas por texto (ID de receta, nombre del paciente, medicamento, etc.)
    public List<Receta> buscarRecetas(String textoBusqueda) throws Exception {
        if (textoBusqueda == null || textoBusqueda.trim().isEmpty()) {
            return listarRecetas(); // Si no hay texto, devolver todas
        }

        try {
            String textoLower = textoBusqueda.toLowerCase().trim();
            List<Receta> todasLasRecetas = listarRecetas();

            return todasLasRecetas.stream()
                    .filter(receta -> coincideConBusqueda(receta, textoLower))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new Exception("Error en la búsqueda de recetas: " + e.getMessage(), e);
        }
    }

    // Método auxiliar para verificar si una receta coincide con el texto de búsqueda
    private boolean coincideConBusqueda(Receta receta, String textoBusqueda) {
        // Buscar en ID de receta
        if (receta.getId() != null && receta.getId().toLowerCase().contains(textoBusqueda)) {
            return true;
        }

        // Buscar en datos del paciente
        if (receta.getPaciente() != null) {
            Paciente paciente = receta.getPaciente();

            // Nombre del paciente
            if (paciente.getNombre() != null &&
                    paciente.getNombre().toLowerCase().contains(textoBusqueda)) {
                return true;
            }

            // ID del paciente
            if (paciente.getId() != null &&
                    paciente.getId().toLowerCase().contains(textoBusqueda)) {
                return true;
            }
        }

        // Buscar en datos del médico
        if (receta.getMedico() != null && receta.getMedico().getNombre() != null &&
                receta.getMedico().getNombre().toLowerCase().contains(textoBusqueda)) {
            return true;
        }

        // Buscar en medicamentos
        if (receta.getDetalles() != null) {
            for (DetalleReceta detalle : receta.getDetalles()) {
                if (detalle.getMedicamento() != null) {
                    Medicamento med = detalle.getMedicamento();

                    // Nombre del medicamento
                    if (med.getNombre() != null &&
                            med.getNombre().toLowerCase().contains(textoBusqueda)) {
                        return true;
                    }

                    // Código del medicamento
                    if (med.getCodigo() != null &&
                            med.getCodigo().toLowerCase().contains(textoBusqueda)) {
                        return true;
                    }

                    // Presentación
                    if (med.getPresentacion() != null &&
                            med.getPresentacion().toLowerCase().contains(textoBusqueda)) {
                        return true;
                    }
                }
            }
        }

        // Buscar en estado
        if (receta.getEstado() != null &&
                receta.getEstado().name().toLowerCase().contains(textoBusqueda)) {
            return true;
        }

        return false;
    }
    // CONSULTAS
    public Receta buscarReceta(String id) throws Exception {
        return recetaLogica.buscarPorId(id);
    }

    public List<Receta> buscarPorPaciente(String id) throws Exception {
        return recetaLogica.buscarPorPaciente(id);
    }

    public List<Receta> listarRecetas() {
        return recetaLogica.listar();
    }

    private void validarMedico(Medico medico) throws Exception {
        if (medico == null) throw new Exception("Solo los médicos pueden ejecutar esta acción.");
    }

    private void validarFarmaceuta(Farmaceuta farmaceuta) throws Exception {
        if (farmaceuta == null) throw new Exception("Solo los farmaceutas pueden ejecutar esta acción.");
    }



}