package hospital.datos.conector;

import jakarta.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import hospital.datos.entidades.PacienteEntidad;

@XmlRootElement(name = "PacientesData")
@XmlAccessorType(XmlAccessType.FIELD)
public class PacienteConector {
    @XmlElementWrapper(name = "Pacientes")
    @XmlElement(name = "Paciente")
    private List<PacienteEntidad> pacientes = new ArrayList<>();

    public List<PacienteEntidad> getPacientes() {
        return pacientes;
    }
    public void setPacientes(List<PacienteEntidad> pacientes) {
        this.pacientes = pacientes;
    }
}
