package hospital.logica.mapper;

import hospital.datos.entidades.PacienteEntidad;
import hospital.model.Paciente;

public class PacienteMapper {
    public static PacienteEntidad toXML(Paciente data) {
        if (data == null) return null;
        PacienteEntidad entity = new PacienteEntidad();
        entity.setId(data.getId());
        entity.setNombre(data.getNombre());
        entity.setTelefono(data.getTelefono());
        entity.setFechaNacimiento(data.getFechaNacimiento());
        return entity;
    }

    public static Paciente fromXML(PacienteEntidad entity) {
        if (entity == null) return null;
        return new Paciente(
                entity.getId(),
                entity.getNombre(),
                entity.getFechaNacimiento(),
                entity.getTelefono()
        );
    }
}