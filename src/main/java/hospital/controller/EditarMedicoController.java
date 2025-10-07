package hospital.controller;

import hospital.Intermediaria.MedicoIntermediaria;
import hospital.model.Administrador;
import hospital.model.Medico;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditarMedicoController {

    @FXML
    private TextField txtIdentificacion;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtEspecialidad;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnVolver;

    private final MedicoIntermediaria medicoIntermediaria = new MedicoIntermediaria();
    private final Administrador admin = new Administrador();
    private Medico medicoActual;

    @FXML
    public void initialize() {
        // El ID no debe ser editable en la modificación
        txtIdentificacion.setEditable(false);
        txtIdentificacion.setStyle("-fx-background-color: #f0f0f0;");
    }

    public void inicializarConMedico(Medico medico) {
        if (medico == null) {
            mostrarError("No se pudo cargar la información del médico.");
            return;
        }

        this.medicoActual = medico;

        // Cargar los datos en los campos
        txtIdentificacion.setText(medico.getId());
        txtNombre.setText(medico.getNombre());
        txtEspecialidad.setText(medico.getEspecialidad());
    }

    @FXML
    public void Guardar(ActionEvent event) {
        try {
            // Validar campos
            if (!validarCampos()) {
                return;
            }

            // Crear médico con los datos actualizados
            Medico medicoModificado = new Medico();
            medicoModificado.setId(txtIdentificacion.getText().trim());
            medicoModificado.setNombre(txtNombre.getText().trim());
            medicoModificado.setEspecialidad(txtEspecialidad.getText().trim());

            // Si el médico original tenía clave, la mantenemos
            if (medicoActual.getClave() != null) {
                medicoModificado.setClave(medicoActual.getClave());
            }

            medicoIntermediaria.modificar(admin, medicoModificado);

            mostrarInfo("Médico modificado exitosamente.");

            volverABusqueda();

        } catch (Exception e) {
            mostrarError("Error al guardar médico: " + e.getMessage());
        }
    }

    @FXML
    public void Volver(ActionEvent event) {
        volverABusqueda();
    }

    private boolean validarCampos() {
        StringBuilder errores = new StringBuilder();

        if (txtIdentificacion.getText() == null || txtIdentificacion.getText().trim().isEmpty()) {
            errores.append("- El ID es obligatorio.\n");
        }

        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            errores.append("- El nombre es obligatorio.\n");
        }

        if (txtEspecialidad.getText() == null || txtEspecialidad.getText().trim().isEmpty()) {
            errores.append("- La especialidad es obligatoria.\n");
        }

        if (errores.length() > 0) {
            mostrarError("Por favor corrija los siguientes errores:\n\n" + errores.toString());
            return false;
        }

        return true;
    }

    private void volverABusqueda() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/medicosAdmin.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Buscar Médicos");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al volver a la búsqueda de médicos.");
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}