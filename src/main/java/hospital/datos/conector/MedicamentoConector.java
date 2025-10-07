package hospital.datos.conector;

import hospital.datos.entidades.MedicamentoEntidad;
import hospital.model.Medicamento;
import jakarta.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "MedicamentosData")
@XmlAccessorType(XmlAccessType.FIELD)
public class MedicamentoConector {

    @XmlElementWrapper(name = "Medicamentos")
    @XmlElement(name = "Medicamento")
    private List<MedicamentoEntidad> medicamentos = new ArrayList<>();

    public List<MedicamentoEntidad> getMedicamentos() { return medicamentos; }
    public void setMedicamentos(List<MedicamentoEntidad> medicamentos) { this.medicamentos = medicamentos; }
}
