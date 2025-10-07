package hospital.logica.mapper;

import hospital.model.DetalleReceta;
import hospital.datos.entidades.DetalleRecetaEntidad;

public class DetalleRecetaMapper {
    public static DetalleRecetaEntidad toXML(DetalleReceta detalle) {
        if (detalle == null || detalle.getMedicamento() == null) return null;

        DetalleRecetaEntidad entity = new DetalleRecetaEntidad();
        entity.setMedicamentoId(detalle.getMedicamento().getCodigo());
        entity.setCantidad(detalle.getCantidad());
        entity.setIndicaciones(detalle.getIndicaciones());
        return entity;
    }

    public static DetalleReceta fromXML(DetalleRecetaEntidad entity) {
        if (entity == null) return null;

        DetalleReceta detalle = new DetalleReceta();
        // Aquí se resolvería el medicamento completo a partir del ID usando algún repositorio
        detalle.setMedicamento(null); // placeholder
        detalle.setCantidad(entity.getCantidad());
        detalle.setIndicaciones(entity.getIndicaciones());

        return detalle;
    }
}
