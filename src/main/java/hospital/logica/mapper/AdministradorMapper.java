package hospital.logica.mapper;

import hospital.datos.entidades.AdministradorEntidad;
import hospital.model.Administrador;

public class AdministradorMapper {
    public static Administrador fromXML(AdministradorEntidad e) {
        if (e == null) return null;
        return new Administrador(e.getId(), e.getClave(), e.getNombre());
    }

    public static AdministradorEntidad toXML(Administrador a) {
        if (a == null) return null;
        AdministradorEntidad e = new AdministradorEntidad();
        e.setId(a.getId());
        e.setClave(a.getClave());
        e.setNombre(a.getNombre());
        return e;
    }
}