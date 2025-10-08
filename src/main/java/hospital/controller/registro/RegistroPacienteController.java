package hospital.controller.registro;

import hospital.logica.PacienteLogica;
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

public class RegistroPacienteController {

    // Corregir los nombres de las variables para que coincidan con los IDs del FXML
    @FXML private TextField txtIdentificacion;
    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono; // El FXML no tiene fx:id para este campo, necesita agregarse
    @FXML private DatePicker dtpFechaNac;

    @FXML private Button btnGuardar; // El FXML no tiene fx:id, necesita agregarse
    @FXML private Button btnVolver;

    private final PacienteLogica pacienteIntermediaria = new PacienteLogica();
    private final Administrador admin = new Administrador(); // Puedes pasar el admin logueado

    @FXML
    public void initialize() {
    }

    @FXML
    public void Guardar(ActionEvent event) {
        try {
            // Validar campos
            if (!validarCampos()) {
                return;
            }

            Paciente nuevoPaciente = new Paciente();
            nuevoPaciente.setId(txtIdentificacion.getText().trim());
            nuevoPaciente.setNombre(txtNombre.getText().trim());
            nuevoPaciente.setTelefono(txtTelefono.getText().trim());
            nuevoPaciente.setFechaNacimiento(dtpFechaNac.getValue());

            pacienteIntermediaria.agregar(admin, nuevoPaciente);

            mostrarInfo("Paciente registrado exitosamente.\nID: " + nuevoPaciente.getId() +
                    "\nNombre: " + nuevoPaciente.getNombre() +
                    "\nTeléfono: " + nuevoPaciente.getTelefono() +
                    "\nFecha de nacimiento: " + nuevoPaciente.getFechaNacimiento());

            // Limpiar formulario después de guardar
            limpiarCampos();

            // Volver a búsqueda como en RegistroMedicoView
            volverABusqueda();

        } catch (Exception e) {
            mostrarError("Error al registrar paciente: " + e.getMessage());
        }
    }

    @FXML
    public void Volver(ActionEvent event) {
        volverABusqueda();
    }

    private boolean validarCampos() {
        StringBuilder errores = new StringBuilder();

        String id = txtIdentificacion.getText();
        String nombre = txtNombre.getText();
        String telefono = txtTelefono.getText();
        LocalDate fecha = dtpFechaNac.getValue();

        // Validaciones básicas como en las otras clases
        if (id == null || id.trim().isEmpty()) {
            errores.append("- El ID es obligatorio.\n");
        } else if (id.trim().length() < 2) {
            errores.append("- El ID debe tener al menos 2 caracteres.\n");
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            errores.append("- El nombre es obligatorio.\n");
        } else if (nombre.trim().length() < 2) {
            errores.append("- El nombre debe tener al menos 2 caracteres.\n");
        }

        if (telefono == null || telefono.trim().isEmpty()) {
            errores.append("- El teléfono es obligatorio.\n");
        }

        if (fecha == null) {
            errores.append("- La fecha de nacimiento es obligatoria.\n");
        } else if (fecha.isAfter(LocalDate.now())) {
            errores.append("- La fecha de nacimiento no puede ser futura.\n");
        }

        if (errores.length() > 0) {
            mostrarError("Por favor corrija los siguientes errores:\n\n" + errores.toString());
            return false;
        }

        return true;
    }

    private void limpiarCampos() {
        txtIdentificacion.clear();
        txtNombre.clear();
        txtTelefono.clear();
        dtpFechaNac.setValue(null);

        // Poner foco en el primer campo
        txtIdentificacion.requestFocus();
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
        alert.setTitle("Registro Exitoso");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

}