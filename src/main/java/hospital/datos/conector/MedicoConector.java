package hospital.datos.conector;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import hospital.datos.entidades.MedicoEntidad;

@XmlRootElement(name = "MedicosData")
@XmlAccessorType(XmlAccessType.FIELD)
public class MedicoConector {

    @XmlElementWrapper(name = "Medicos")
    @XmlElement(name = "Medico")
    private List<MedicoEntidad> medicos = new ArrayList<>();

    public List<MedicoEntidad> getMedicos() { return medicos; }
    public void setMedicos(List<MedicoEntidad> medicos) { this.medicos = medicos; }
}
