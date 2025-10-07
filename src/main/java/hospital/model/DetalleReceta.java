package hospital.model;

public class DetalleReceta {
    private Medicamento medicamento;
    private int cantidad;
    private String indicaciones;// ej: "Tomar cada 8 horas"
    private int diasTratamiento;

    public DetalleReceta() {}

    public DetalleReceta(Medicamento medicamento, int cantidad, String indicaciones, int diasTratamiento) throws Exception {
        if (medicamento == null) throw new Exception("El medicamento no puede ser nulo.");
        if (cantidad <= 0) throw new Exception("La cantidad debe ser mayor que 0.");
        if (indicaciones == null || indicaciones.isBlank()) throw new Exception("Las indicaciones son obligatorias.");
        if (diasTratamiento <= 0) throw new Exception("Los días de tratamiento deben ser mayores que 0.");

        this.medicamento = medicamento;
        this.cantidad = cantidad;
        this.indicaciones = indicaciones;
        this.diasTratamiento = diasTratamiento;
    }


    public Medicamento getMedicamento() {
        return medicamento;
    }

    public void setMedicamento(Medicamento medicamento) {
        this.medicamento = medicamento;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public void setIndicaciones(String indicaciones) {
        this.indicaciones = indicaciones;
    }

    public int getDiasTratamiento() { return diasTratamiento; }
    public void setDiasTratamiento(int diasTratamiento) { this.diasTratamiento = diasTratamiento; }

    @Override
    public String toString() {
        return medicamento.getNombre() + " x" + cantidad + " (" + indicaciones + ")";
    }
}
