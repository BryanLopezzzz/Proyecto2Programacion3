package hospital.datos.entidades;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import hospital.model.DetalleReceta;
import hospital.datos.LocalDateAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class RecetaEntidad {
    private String id;
    private String pacienteId; // Guardamos solo el ID
    private String medicoId;   // Guardamos solo el ID
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate fecha;
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate fechaRetiro;
    private String estado;

    @XmlElementWrapper(name = "Detalles")
    @XmlElement(name = "Detalle")
    private List<DetalleReceta> detalles = new ArrayList<>();

    public RecetaEntidad() {}

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }

    public String getMedicoId() { return medicoId; }
    public void setMedicoId(String medicoId) { this.medicoId = medicoId; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalDate getFechaRetiro() { return fechaRetiro; }
    public void setFechaRetiro(LocalDate fechaRetiro) { this.fechaRetiro = fechaRetiro; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<DetalleReceta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleReceta> detalles) { this.detalles = detalles; }
}
