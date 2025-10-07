package hospital.datos.conector;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import hospital.datos.entidades.AdministradorEntidad;

@XmlRootElement(name = "AdministradoresData")
@XmlAccessorType(XmlAccessType.FIELD)
public class AdministradorConector {

    @XmlElementWrapper(name = "Administradores")
    @XmlElement(name = "Administrador")
    private List<AdministradorEntidad> administradores = new ArrayList<>();

    public List<AdministradorEntidad> getAdministradores() { return administradores; }
    public void setAdministradores(List<AdministradorEntidad> administradores) { this.administradores = administradores; }
}
