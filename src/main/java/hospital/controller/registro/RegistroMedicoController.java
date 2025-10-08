package hospital.controller.registro;


import hospital.logica.MedicoLogica;
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

public class RegistroMedicoController {

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

    private final MedicoLogica medicoIntermediaria = new MedicoLogica();
    private final Administrador admin = new Administrador();

    @FXML
    public void initialize() {
    }

    @FXML
    public void Guardar(ActionEvent event) {
        try {
            if (!validarCampos()) {
                return;
            }

            Medico nuevoMedico = new Medico();
            nuevoMedico.setId(txtIdentificacion.getText().trim());
            nuevoMedico.setNombre(txtNombre.getText().trim());
            nuevoMedico.setEspecialidad(txtEspecialidad.getText().trim());


            medicoIntermediaria.agregar(admin, nuevoMedico);

            mostrarInfo("Médico registrado exitosamente.\nID: " + nuevoMedico.getId() +
                    "\nNombre: " + nuevoMedico.getNombre() +
                    "\nEspecialidad: " + nuevoMedico.getEspecialidad());

            limpiarCampos();
            volverABusqueda();

        } catch (Exception e) {
            mostrarError("Error al registrar médico: " + e.getMessage());
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
        String especialidad = txtEspecialidad.getText();

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

        if (especialidad == null || especialidad.trim().isEmpty()) {
            errores.append("- La especialidad es obligatoria.\n");
        } else if (especialidad.trim().length() < 3) {
            errores.append("- La especialidad debe tener al menos 3 caracteres.\n");
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
        txtEspecialidad.clear();

        // Enfocar el primer campo para facilitar el ingreso de datos
        txtIdentificacion.requestFocus();
    }

    private void volverABusqueda() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/MedicosAdmin.fxml"));
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
        alert.setTitle("Registro Exitoso");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    /*
    public void precargarDatos(String id, String nombre, String especialidad) {
        if (id != null && !id.isEmpty()) {
            txtIdentificacion.setText(id);
        }
        if (nombre != null && !nombre.isEmpty()) {
            txtNombre.setText(nombre);
        }
        if (especialidad != null && !especialidad.isEmpty()) {
            txtEspecialidad.setText(especialidad);
        }
    }
    */

}