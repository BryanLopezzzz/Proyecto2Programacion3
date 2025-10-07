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
        if (receta != null) {
            if (receta.getPaciente() != null) {
                lblPacienteId.setText(receta.getPaciente().getId());
                lblPacienteNombre.setText(receta.getPaciente().getNombre());
                lblPacienteNacimiento.setText(receta.getPaciente().getFechaNacimiento().toString());
                lblPacienteTelefono.setText(receta.getPaciente().getTelefono());
            }
            if (receta.getMedico() != null) {
                lblMedicoId.setText(receta.getMedico().getId());
                lblMedicoNombre.setText(receta.getMedico().getNombre());
                lblMedicoEspecialidad.setText(receta.getMedico().getEspecialidad());
            }
            if (receta.getDetalles() != null && !receta.getDetalles().isEmpty()) {
                lblMedicamentoNombre.setText(receta.getPrimerMedicamento());
                lblMedicamentoPresentacion.setText(receta.getPresentacionPrimerMedicamento());
                lblMedicamentoCantidad.setText(String.valueOf(receta.getDetalles().get(0).getCantidad()));
                lblMedicamentoDuracion.setText(String.valueOf(receta.getDetalles().get(0).getDiasTratamiento()));
                lblIndicaciones.setText(receta.getDetalles().get(0).getIndicaciones());
            }
            lblRecetaConfeccion.setText(receta.getFechaConfeccion().toString());
            lblRecetaRetiro.setText(receta.getFechaRetiro().toString());
            lblRecetaEstado.setText(receta.getEstado().toString());
        }
    }

    @FXML
    private void Volver(ActionEvent event) {
        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.close();
    }
}
