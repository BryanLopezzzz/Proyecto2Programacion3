package hospital.logica.mapper;

import hospital.model.Medicamento;
import hospital.datos.entidades.MedicamentoEntidad;

public class MedicamentoMapper {

    public static MedicamentoEntidad toXML(Medicamento data) {
        if (data == null) return null;
        MedicamentoEntidad entity = new MedicamentoEntidad();
        entity.setCodigo(data.getCodigo());
        entity.setNombre(data.getNombre());
        entity.setPresentacion(data.getPresentacion());
        return entity;
    }

    public static Medicamento fromXML(MedicamentoEntidad entity) {
        if (entity == null) return null;
        return new Medicamento(
                entity.getCodigo(),
                entity.getNombre(),
                entity.getPresentacion()
        );
    }
}
