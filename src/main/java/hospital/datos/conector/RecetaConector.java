package hospital.datos.conector;

import jakarta.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import hospital.datos.entidades.RecetaEntidad;

@XmlRootElement(name = "RecetasData")
@XmlAccessorType(XmlAccessType.FIELD)
public class RecetaConector {
    @XmlElementWrapper(name = "Recetas")
    @XmlElement(name = "Receta")
    private List<RecetaEntidad> recetas = new ArrayList<>();

    public List<RecetaEntidad> getRecetas() { return recetas; }
    public void setRecetas(List<RecetaEntidad> recetas) { this.recetas = recetas; }
}
