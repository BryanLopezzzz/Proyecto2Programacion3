package hospital.logica.mapper;

import hospital.datos.entidades.FarmaceutaEntidad;
import hospital.model.Farmaceuta;
public class FarmaceutaMapper {

    public static FarmaceutaEntidad toXML(Farmaceuta data) {
        if (data == null) return null;
        FarmaceutaEntidad e = new FarmaceutaEntidad();
        e.setId(data.getId());
        e.setClave(data.getClave());
        e.setNombre(data.getNombre());
        return e;
    }

    public static Farmaceuta fromXML(FarmaceutaEntidad e) {
        if (e == null) return null;
        return new Farmaceuta(
                e.getId(),
                e.getClave(),
                e.getNombre()
        );
    }
}
