package hospital.logica.mapper;

import hospital.datos.entidades.MedicoEntidad;
import hospital.model.Medico;

public class MedicoMapper {
    public static MedicoEntidad toXML(Medico data) {
        if (data == null) return null;
        MedicoEntidad e = new MedicoEntidad();
        e.setId(data.getId());
        e.setClave(data.getClave());
        e.setNombre(data.getNombre());
        e.setEspecialidad(data.getEspecialidad());
        return e;
    }

    public static Medico fromXML(MedicoEntidad e) {
        if (e == null) return null;
        return new Medico(
                e.getId(),
                e.getClave(),
                e.getNombre(),
                e.getEspecialidad()
        );
    }
}
