package hospital.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Receta {
    private String id;                // consecutivo único
    private Paciente paciente;        // paciente asociado
    private Medico medico;            // médico que la emite
    private LocalDate fecha;
    private LocalDate fechaRetiro;
    private EstadoReceta estado;
    private List<DetalleReceta> detalles; // medicamentos prescritos

    public Receta() {
        this.detalles = new ArrayList<>();
        this.estado = EstadoReceta.CONFECCIONADA;
        this.fecha = LocalDate.now();
        this.fechaRetiro = this.fecha;
    }

    public Receta(String id, Paciente paciente, Medico medico, LocalDate fecha) {
        this.id = id;
        this.paciente = paciente;
        this.medico = medico;
        this.fecha = (fecha != null ? fecha : LocalDate.now());
        this.estado = EstadoReceta.CONFECCIONADA;
        this.detalles = new ArrayList<>();
        this.fechaRetiro = this.fecha;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public Paciente getPaciente() {
        return paciente;
    }
    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Medico getMedico() {
        return medico;
    }
    public void setMedico(Medico medico) {
        this.medico = medico;
    }

    public LocalDate getFecha() {
        return fecha;
    }
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public EstadoReceta getEstado() {
        return estado;
    }
    public void setEstado(EstadoReceta estado) {
        this.estado = estado;
    }

    public List<DetalleReceta> getDetalles() {
        return detalles;
    }
    public void setDetalles(List<DetalleReceta> detalles) {
        this.detalles = detalles;
    }

    public void agregarDetalle(DetalleReceta detalle) {
        detalles.add(detalle);
    }

    public LocalDate getFechaRetiro() {
        return fechaRetiro;
    }
    public void setFechaRetiro(LocalDate fechaRetiro) {
        this.fechaRetiro = fechaRetiro;
    }

    public String getPrimerMedicamento() {
        if (detalles != null && !detalles.isEmpty()) {
            Medicamento med = detalles.get(0).getMedicamento();
            return med != null ? med.getNombre() : "N/A";
        }
        return "N/A";
    }

    public String getPresentacionPrimerMedicamento() {
        if (detalles != null && !detalles.isEmpty()) {
            Medicamento med = detalles.get(0).getMedicamento();
            return med != null ? med.getPresentacion() : "N/A";
        }
        return "N/A";
    }

    public LocalDate getFechaConfeccion() {
        return fecha;
    }

    @Override
    public String toString() {
        return "Receta " + id + " - " + (paciente != null ? paciente.getNombre() : "Paciente?")
                + " (" + estado + ") - Retiro: " + (fechaRetiro != null ? fechaRetiro : "¿?");
    }

    /////////////////////////////////////////////////
    /// //Proceso:
    ///     // Médico → confeccionada
    ///     //
    ///     //Farmaceuta en ventanilla → en proceso
    ///     //
    ///     //Farmaceuta alista → lista
    ///     //
    ///     //Farmaceuta entrega → entregada
    ///////////////////////////////////////////////////
}
