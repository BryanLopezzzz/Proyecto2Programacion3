package hospital.logica;

import hospital.model.DetalleReceta;
import hospital.model.Medicamento;

public class DetalleRecetaLogica {

    public static DetalleReceta crearDetalle(String medicamentoId, int cantidad, String indicaciones,int diasTratamiento, MedicamentoLogica medicamentoLogica) throws Exception {
        Medicamento medicamento = medicamentoLogica.buscarPorCodigo(medicamentoId);
        if (medicamento == null) throw new Exception("Medicamento no encontrado: " + medicamentoId);

        // Validar
        if (cantidad <= 0) throw new Exception("La cantidad debe ser mayor que 0.");
        if (diasTratamiento <= 0) {
            throw new Exception("Los días de tratamiento deben ser mayores que 0.");
        }
        if (indicaciones == null || indicaciones.isBlank()) throw new Exception("Las indicaciones son obligatorias.");

        return new DetalleReceta(medicamento, cantidad, indicaciones,diasTratamiento);
    }
}
