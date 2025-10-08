package hospital.controller;

import hospital.logica.FarmaceutaLogica;
import hospital.model.Administrador;
import hospital.model.Farmaceuta;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EditarFarmaceutaController {

    @FXML
    private TextField txtIdentificacionFarmaceuta;

    @FXML
    private TextField txtNombreFarmaceuta;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnVolver;

    private final FarmaceutaLogica farmaceutaIntermediaria = new FarmaceutaLogica();
    private final Administrador admin = new Administrador(); // Se debe pasar el admin logueado

    private Farmaceuta farmaceutaOriginal; // Para almacenar el farmaceuta que se está editando

    @FXML
    public void initialize() {
        configurarValidaciones();
        configurarCampos();
    }

    private void configurarValidaciones() {
        // Validación para nombre (solo letras y espacios)
        txtNombreFarmaceuta.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*")) {
                txtNombreFarmaceuta.setText(oldValue);
            }
        });
    }

    private void configurarCampos() {
        // El ID no se puede editar en modo edición
        txtIdentificacionFarmaceuta.setEditable(false);
        txtIdentificacionFarmaceuta.setStyle(txtIdentificacionFarmaceuta.getStyle() + "; -fx-background-color: #f0f0f0;");

        // Enfocar el campo nombre por defecto
        txtNombreFarmaceuta.requestFocus();
    }

    public void cargarFarmaceuta(Farmaceuta farmaceuta) {
        if (farmaceuta == null) {
            mostrarError("No se pudo cargar el farmaceuta para edición.");
            return;
        }

        this.farmaceutaOriginal = farmaceuta;

        // Cargar los datos en los campos
        txtIdentificacionFarmaceuta.setText(farmaceuta.getId());
        txtNombreFarmaceuta.setText(farmaceuta.getNombre());
    }

    @FXML
    public void Guardar(ActionEvent event) {
        if (!validarCampos()) {
            return;
        }

        // Verificar si hubo cambios
        String nuevoNombre = txtNombreFarmaceuta.getText().trim();
        if (farmaceutaOriginal != null && nuevoNombre.equals(farmaceutaOriginal.getNombre())) {
            mostrarInfo("No se detectaron cambios para guardar.");
            return;
        }

        // Mostrar confirmación antes de guardar
        mostrarConfirmacion("¿Está seguro que desea guardar los cambios?", () -> {
            try {
                // Crear farmaceuta actualizado
                Farmaceuta farmaceutaActualizado = new Farmaceuta();
                farmaceutaActualizado.setId(txtIdentificacionFarmaceuta.getText().trim());
                farmaceutaActualizado.setNombre(nuevoNombre);

                // Si el farmaceuta original tiene clave, mantenerla
                if (farmaceutaOriginal != null && farmaceutaOriginal.getClave() != null) {
                    farmaceutaActualizado.setClave(farmaceutaOriginal.getClave());
                }

                // Actualizar usando el controller
                farmaceutaIntermediaria.modificar(admin, farmaceutaActualizado);

                // Mostrar mensaje de éxito
                mostrarInfo("Farmaceuta actualizado exitosamente.");

                // Volver a la vista de búsqueda
                volverABusqueda();

            } catch (Exception e) {
                mostrarError("Error al actualizar farmaceuta: " + e.getMessage());
            }
        });
    }

    @FXML
    public void Volver(ActionEvent event) {
        // Verificar si hay cambios sin guardar
        if (hayCambiosSinGuardar()) {
            mostrarConfirmacion("Hay cambios sin guardar. ¿Está seguro que desea salir?",
                    this::volverABusqueda);
        } else {
            volverABusqueda();
        }
    }

    private boolean validarCampos() {
        StringBuilder errores = new StringBuilder();

        // Validar que el ID no esté vacío (aunque no sea editable)
        String id = txtIdentificacionFarmaceuta.getText().trim();
        if (id.isEmpty()) {
            errores.append("- El ID no puede estar vacío.\n");
        }

        // Validar nombre
        String nombre = txtNombreFarmaceuta.getText().trim();
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

    private boolean hayCambiosSinGuardar() {
        if (farmaceutaOriginal == null) {
            return false;
        }

        String nombreActual = txtNombreFarmaceuta.getText().trim();
        return !nombreActual.equals(farmaceutaOriginal.getNombre());
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

    // Metodo getter para testing o uso externo
    public Farmaceuta getFarmaceutaOriginal() {
        return farmaceutaOriginal;
    }
}