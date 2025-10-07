package hospital.controller;

import hospital.Intermediaria.PacienteIntermediaria;
import hospital.model.Administrador;
import hospital.model.Paciente;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EditarPacienteController {

    @FXML
    private TextField txtIdentificacion;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtTelefono;

    @FXML
    private DatePicker dtpFechaNac;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnVolver;

    private final PacienteIntermediaria pacienteIntermediaria = new PacienteIntermediaria();
    private final Administrador admin = new Administrador();
    private Paciente pacienteOriginal;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        // Hacer el campo ID no editable (solo lectura)
        txtIdentificacion.setEditable(false);
        txtIdentificacion.setStyle(txtIdentificacion.getStyle() + "; -fx-background-color: #f5f5f5;");
    }

    public void cargarPaciente(Paciente paciente) {
        if (paciente != null) {
            this.pacienteOriginal = paciente;
            txtIdentificacion.setText(paciente.getId());
            txtNombre.setText(paciente.getNombre());
            txtTelefono.setText(paciente.getTelefono());
            dtpFechaNac.setValue(paciente.getFechaNacimiento());
        }
    }

    @FXML
    public void Guardar(ActionEvent event) {
        try {
            // Validar que los campos no estén vacíos
            if (!validarCampos()) {
                return;
            }

            // Crear objeto paciente actualizado
            Paciente pacienteActualizado = new Paciente();
            pacienteActualizado.setId(txtIdentificacion.getText().trim());
            pacienteActualizado.setNombre(txtNombre.getText().trim());
            pacienteActualizado.setTelefono(txtTelefono.getText().trim());
            pacienteActualizado.setFechaNacimiento(dtpFechaNac.getValue());

            // Verificar que se hicieron cambios
            if (sonIguales(pacienteOriginal, pacienteActualizado)) {
                mostrarInfo("No se han detectado cambios en los datos del paciente.");
                return;
            }

            // Actualizar paciente usando el controller
            pacienteIntermediaria.modificar(admin, pacienteActualizado);

            mostrarInfo("Paciente actualizado correctamente.");

            // Volver a la vista anterior después de guardar exitosamente
            volverABusqueda();

        } catch (Exception e) {
            mostrarError("Error al actualizar paciente: " + e.getMessage());
        }
    }

    @FXML
    public void Volver(ActionEvent event) {
        volverABusqueda();
    }

    private boolean validarCampos() {
        StringBuilder errores = new StringBuilder();

        if (txtNombre.getText().trim().isEmpty()) {
            errores.append("- El nombre es obligatorio.\n");
        }

        if (txtTelefono.getText().trim().isEmpty()) {
            errores.append("- El teléfono es obligatorio.\n");
        }

        if (dtpFechaNac.getValue() == null) {
            errores.append("- La fecha de nacimiento es obligatoria.\n");
        } else if (dtpFechaNac.getValue().isAfter(LocalDate.now())) {
            errores.append("- La fecha de nacimiento no puede ser futura.\n");
        }

        if (errores.length() > 0) {
            mostrarError("Por favor corrija los siguientes errores:\n\n" + errores.toString());
            return false;
        }

        return true;
    }

    private boolean sonIguales(Paciente original, Paciente actualizado) {
        if (original == null || actualizado == null) return false;

        return original.getId().equals(actualizado.getId()) &&
                original.getNombre().equals(actualizado.getNombre()) &&
                original.getTelefono().equals(actualizado.getTelefono()) &&
                original.getFechaNacimiento().equals(actualizado.getFechaNacimiento());
    }

    private void volverABusqueda() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/pacientesAdmin.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Buscar Pacientes");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al volver a la vista de búsqueda.");
        }
    }

    // Métodos utilitarios para mostrar alertas
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Ha ocurrido un error");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText("Operación exitosa");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}