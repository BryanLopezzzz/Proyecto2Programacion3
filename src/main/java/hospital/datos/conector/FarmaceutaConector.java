package hospital.datos.conector;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import hospital.datos.entidades.FarmaceutaEntidad;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "FarmaceutasData")
@XmlAccessorType(XmlAccessType.FIELD)
public class FarmaceutaConector {

    @XmlElementWrapper(name = "Farmaceutas")
    @XmlElement(name = "Farmaceuta")
    private List<FarmaceutaEntidad> farmaceutas = new ArrayList<>();

    public List<FarmaceutaEntidad> getFarmaceutas() { return farmaceutas; }
    public void setFarmaceutas(List<FarmaceutaEntidad> farmaceutas) { this.farmaceutas = farmaceutas; }
}