package hospital.logica.mapper;

import hospital.datos.entidades.RecetaEntidad;
import hospital.logica.PacienteLogica;
import hospital.model.*;
import hospital.logica.MedicoLogica;

import java.util.List;
import java.util.stream.Collectors;

public class RecetaMapper {

    // Convierte modelo → entidad XML
    public static RecetaEntidad toXML(Receta receta) {
        if (receta == null) return null;

        RecetaEntidad entity = new RecetaEntidad();
        entity.setId(receta.getId());
        entity.setPacienteId(receta.getPaciente() != null ? receta.getPaciente().getId() : null);
        entity.setMedicoId(receta.getMedico() != null ? receta.getMedico().getId() : null);
        entity.setFecha(receta.getFecha());
        entity.setFechaRetiro(receta.getFechaRetiro());
        entity.setEstado(receta.getEstado().name());
        entity.setDetalles(receta.getDetalles());

        return entity;
    }

    public static Receta fromXML(RecetaEntidad entity,
                                 PacienteLogica pacienteLogica,
                                 MedicoLogica medicoLogica,
                                 boolean strict) {
        if (entity == null) return null;

        Receta receta = new Receta();
        receta.setId(entity.getId());

        // Resolver paciente
        if (entity.getPacienteId() != null && pacienteLogica != null) {
            Paciente pacientes = pacienteLogica.buscarPorId(entity.getPacienteId());

            if (pacientes == null && strict) {
                throw new RuntimeException("Paciente no encontrado: " + entity.getPacienteId());
            }
            receta.setPaciente(pacientes);
        } else {
            receta.setPaciente(null);
        }

        // Resolver médico
        if (entity.getMedicoId() != null && medicoLogica != null) {
            Medico medicos = medicoLogica.buscarPorId(entity.getMedicoId());;
            if (medicos == null && strict) {
                throw new RuntimeException("Médico no encontrado: " + entity.getMedicoId());
            }
            receta.setMedico(medicos);
        } else {
            receta.setMedico(null);
        }

        receta.setFecha(entity.getFecha());
        receta.setFechaRetiro(entity.getFechaRetiro());
        receta.setEstado(entity.getEstado() != null
                ? EstadoReceta.valueOf(entity.getEstado())
                : EstadoReceta.CONFECCIONADA);
        receta.getDetalles().addAll(entity.getDetalles());

        return receta;
    }

    // Helper para crear detalles de receta
    public static DetalleReceta crearDetalle(Medicamento medicamento, int cantidad, String indicaciones, int diasTratamiento) throws Exception {
        if (medicamento == null) throw new Exception("El medicamento no puede ser nulo.");
        if (cantidad <= 0) throw new Exception("La cantidad debe ser mayor que 0.");
        if (diasTratamiento <= 0) throw new Exception("La cantidad de días debe ser mayor que 0.");
        if (indicaciones == null || indicaciones.isBlank()) throw new Exception("Las indicaciones son obligatorias.");

        return new DetalleReceta(medicamento, cantidad, indicaciones,diasTratamiento);
    }
}
