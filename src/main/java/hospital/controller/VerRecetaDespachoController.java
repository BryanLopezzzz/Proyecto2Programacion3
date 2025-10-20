package hospital.controller;

import hospital.model.Receta;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class VerRecetaDespachoController {

    @FXML
    private Label lblPacienteId;

    @FXML
    private Label lblPacienteNombre;

    @FXML
    private Label lblPacienteNacimiento;

    @FXML
    private Label lblPacienteTelefono;

    @FXML
    private Label lblMedicoId;

    @FXML
    private Label lblMedicoNombre;

    @FXML
    private Label lblMedicoEspecialidad;

    @FXML
    private Label lblMedicamentoNombre;

    @FXML
    private Label lblMedicamentoPresentacion;

    @FXML
    private Label lblMedicamentoCantidad;

    @FXML
    private Label lblMedicamentoDuracion;

    @FXML
    private Label lblRecetaConfeccion;

    @FXML
    private Label lblRecetaRetiro;

    @FXML
    private Label lblRecetaEstado;

    @FXML
    private Label lblIndicaciones;

    @FXML
    private Button btnVolver;

    @FXML
    public void setReceta(Receta receta) {
        if (receta == null) {
            Alerta.error("Error", "No se pudo cargar la receta.");
            return;
        }

        // Datos del paciente
        if (receta.getPaciente() != null) {
            lblPacienteId.setText(receta.getPaciente().getId() != null ?
                    receta.getPaciente().getId() : "-");
            lblPacienteNombre.setText(receta.getPaciente().getNombre() != null ?
                    receta.getPaciente().getNombre() : "-");
            lblPacienteNacimiento.setText(receta.getPaciente().getFechaNacimiento() != null ?
                    receta.getPaciente().getFechaNacimiento().toString() : "-");
            lblPacienteTelefono.setText(receta.getPaciente().getTelefono() != null ?
                    receta.getPaciente().getTelefono() : "-");
        } else {
            lblPacienteId.setText("-");
            lblPacienteNombre.setText("-");
            lblPacienteNacimiento.setText("-");
            lblPacienteTelefono.setText("-");
        }

        // Datos del médico
        if (receta.getMedico() != null) {
            lblMedicoId.setText(receta.getMedico().getId() != null ?
                    receta.getMedico().getId() : "-");
            lblMedicoNombre.setText(receta.getMedico().getNombre() != null ?
                    receta.getMedico().getNombre() : "-");
            lblMedicoEspecialidad.setText(receta.getMedico().getEspecialidad() != null ?
                    receta.getMedico().getEspecialidad() : "-");
        } else {
            lblMedicoId.setText("-");
            lblMedicoNombre.setText("-");
            lblMedicoEspecialidad.setText("-");
        }

        // Datos del medicamento y detalle
        if (receta.getDetalles() != null && !receta.getDetalles().isEmpty()) {
            lblMedicamentoNombre.setText(receta.getPrimerMedicamento() != null ?
                    receta.getPrimerMedicamento() : "-");
            lblMedicamentoPresentacion.setText(receta.getPresentacionPrimerMedicamento() != null ?
                    receta.getPresentacionPrimerMedicamento() : "-");
            lblMedicamentoCantidad.setText(String.valueOf(receta.getDetalles().get(0).getCantidad()));
            lblMedicamentoDuracion.setText(String.valueOf(receta.getDetalles().get(0).getDiasTratamiento()));
            lblIndicaciones.setText(receta.getDetalles().get(0).getIndicaciones() != null ?
                    receta.getDetalles().get(0).getIndicaciones() : "-");
        } else {
            lblMedicamentoNombre.setText("-");
            lblMedicamentoPresentacion.setText("-");
            lblMedicamentoCantidad.setText("-");
            lblMedicamentoDuracion.setText("-");
            lblIndicaciones.setText("-");
        }

        // Datos de la receta
        lblRecetaConfeccion.setText(receta.getFechaConfeccion() != null ?
                receta.getFechaConfeccion().toString() : "-");
        lblRecetaRetiro.setText(receta.getFechaRetiro() != null ?
                receta.getFechaRetiro().toString() : "-");
        lblRecetaEstado.setText(receta.getEstado() != null ?
                receta.getEstado().toString() : "-");
    }

    @FXML
    private void Volver(ActionEvent event) {
        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.close();
    }
}
