package hospital.controller.registro;

import hospital.Intermediaria.FarmaceutaIntermediaria;
import hospital.model.Administrador;
import hospital.model.Farmaceuta;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegistroFarmaceutaController {

    @FXML
    private TextField txtIdentificacion;

    @FXML
    private TextField txtNombre;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnVolver;

    private final FarmaceutaIntermediaria farmaceutaIntermediaria = new FarmaceutaIntermediaria();
    private final Administrador admin = new Administrador(); // Se debe pasar el admin logueado

    @FXML
    public void initialize() {
        // Configuraciones adicionales si son necesarias
        configurarValidaciones();
    }

    private void configurarValidaciones() {
        // Validación en tiempo real para ID (solo números y letras)
        txtIdentificacion.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z0-9]*")) {
                txtIdentificacion.setText(oldValue);
            }
        });

        // Validación para nombre (solo letras y espacios)
        txtNombre.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*")) {
                txtNombre.setText(oldValue);
            }
        });
    }

    @FXML
    public void guardarFarmaceuta(ActionEvent event) {
        if (!validarCampos()) {
            return;
        }

        try {
            Farmaceuta nuevoFarmaceuta = new Farmaceuta();
            nuevoFarmaceuta.setId(txtIdentificacion.getText().trim());
            nuevoFarmaceuta.setNombre(txtNombre.getText().trim());

            farmaceutaIntermediaria.agregar(admin, nuevoFarmaceuta);

            mostrarInfo("Farmaceuta agregado exitosamente.");

            limpiarCampos();

            volverABusqueda();

        } catch (Exception e) {
            mostrarError("Error al agregar farmaceuta: " + e.getMessage());
        }
    }

    @FXML
    public void Volver(ActionEvent event) {
        volverABusqueda();
    }

    private boolean validarCampos() {
        StringBuilder errores = new StringBuilder();

        // Validar ID
        String id = txtIdentificacion.getText().trim();
        if (id.isEmpty()) {
            errores.append("- El ID es obligatorio.\n");
        } else if (id.length() < 3) {
            errores.append("- El ID debe tener al menos 3 caracteres.\n");
        }

        // Validar nombre
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            errores.append("- El nombre es obligatorio.\n");
        } else if (nombre.length() < 2) {
            errores.append("- El nombre debe tener al menos 2 caracteres.\n");
        }

        // Mostrar errores si existen
        if (errores.length() > 0) {
            mostrarError("Por favor corrija los siguientes errores:\n\n" + errores.toString());
            return false;
        }

        return true;
    }

    private void limpiarCampos() {
        txtIdentificacion.clear();
        txtNombre.clear();
        txtIdentificacion.requestFocus();
    }

    private void volverABusqueda() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/farmaceutasAdmin.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestión de Farmaceutas");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al volver a la vista de búsqueda: " + e.getMessage());
        }
    }

    // Método para ser llamado desde otras vistas si se necesita precargar datos
    public void cargarDatos(String id, String nombre) {
        if (id != null) txtIdentificacion.setText(id);
        if (nombre != null) txtNombre.setText(nombre);
    }

    // Método para configurar si es edición (deshabilitar ID)
    public void configurarModoEdicion(boolean esEdicion) {
        txtIdentificacion.setEditable(!esEdicion);
        txtIdentificacion.setDisable(esEdicion);

        if (esEdicion) {
            txtNombre.requestFocus();
        }
    }

    // Métodos utilitarios
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

    private void mostrarConfirmacion(String mensaje, Runnable accion) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        if (alert.showAndWait().get() == ButtonType.OK) {
            accion.run();
        }
    }
}