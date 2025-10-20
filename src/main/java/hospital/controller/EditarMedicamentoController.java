package hospital.controller;

import hospital.controller.busqueda.Async;
import hospital.logica.MedicamentoLogica;
import hospital.model.Administrador;
import hospital.model.Medicamento;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class EditarMedicamentoController implements Initializable {

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtPresentacion;

    @FXML
    private Button btnGuardarMedicamento;

    @FXML
    private Button btnVolver;

    @FXML
    private ProgressIndicator progressIndicator;

    private final MedicamentoLogica medicamentoLogica;
    private final Administrador administrador;
    private Medicamento medicamentoOriginal;

    public EditarMedicamentoController() {
        this.medicamentoLogica = new MedicamentoLogica();
        this.administrador = new Administrador();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // El código del medicamento no debería ser editable
        txtCodigo.setEditable(false);
        txtCodigo.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #b3b3b3; -fx-border-radius: 4;");

        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }

        // Enfocar el campo nombre por defecto
        txtNombre.requestFocus();
    }

    public void setMedicamento(Medicamento medicamento) {
        if (medicamento != null) {
            this.medicamentoOriginal = medicamento;
            txtCodigo.setText(medicamento.getCodigo());
            txtNombre.setText(medicamento.getNombre());
            txtPresentacion.setText(medicamento.getPresentacion());
        }
    }

    @FXML
    private void Guardar() {
        if (!validarCampos()) {
            return;
        }

        // Verificar si hubo cambios
        if (!hayCambios()) {
            mostrarInformacion("No se detectaron cambios para guardar.");
            return;
        }

        // Mostrar confirmación antes de guardar
        mostrarConfirmacion("¿Está seguro que desea guardar los cambios?", this::guardarMedicamentoAsync);
    }

    private void guardarMedicamentoAsync() {
        deshabilitarControles(true);
        mostrarCargando(true);

        Async.runVoid(
                () -> {
                    try {
                        // Crear medicamento con los nuevos datos
                        Medicamento medicamentoEditado = new Medicamento(
                                medicamentoOriginal.getCodigo(),
                                txtNombre.getText().trim(),
                                txtPresentacion.getText().trim()
                        );

                        medicamentoLogica.modificar(administrador, medicamentoEditado);
                    } catch (Exception e) {
                        throw new RuntimeException("Error al actualizar: " + e.getMessage(), e);
                    }
                },
                // OnSuccess
                () -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    mostrarInformacion("Medicamento actualizado correctamente.");
                    Volver();
                },
                // OnError
                error -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    mostrarError("Error al actualizar medicamento: " + error.getMessage());
                }
        );
    }

    @FXML
    private void Volver() {
        // Verificar si hay cambios sin guardar
        if (hayCambios()) {
            mostrarConfirmacion("Hay cambios sin guardar. ¿Está seguro que desea salir?",
                    this::volverABusqueda);
        } else {
            volverABusqueda();
        }
    }

    private void volverABusqueda() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/MedicamentosAdmin.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Buscar Medicamentos");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al volver a la vista de búsqueda: " + e.getMessage());
        }
    }

    private boolean validarCampos() {
        StringBuilder errores = new StringBuilder();

        // Validar código (aunque no sea editable)
        String codigo = txtCodigo.getText().trim();
        if (codigo.isEmpty()) {
            errores.append("- El código no puede estar vacío.\n");
        }

        // Validar nombre
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            errores.append("- El nombre es obligatorio.\n");
        } else if (nombre.length() < 2) {
            errores.append("- El nombre debe tener al menos 2 caracteres.\n");
        }

        // Validar presentación
        String presentacion = txtPresentacion.getText().trim();
        if (presentacion.isEmpty()) {
            errores.append("- La presentación es obligatoria.\n");
        } else if (presentacion.length() < 2) {
            errores.append("- La presentación debe tener al menos 2 caracteres.\n");
        }

        // Validar que exista medicamento original
        if (medicamentoOriginal == null) {
            errores.append("- Error: No se ha establecido el medicamento a editar.\n");
        }

        // Mostrar errores si existen
        if (errores.length() > 0) {
            mostrarError("Por favor corrija los siguientes errores:\n\n" + errores.toString());
            return false;
        }

        return true;
    }

    private boolean hayCambios() {
        if (medicamentoOriginal == null) {
            return false;
        }

        String nombreActual = txtNombre.getText().trim();
        String presentacionActual = txtPresentacion.getText().trim();

        return !nombreActual.equals(medicamentoOriginal.getNombre()) ||
                !presentacionActual.equals(medicamentoOriginal.getPresentacion());
    }

    private void deshabilitarControles(boolean deshabilitar) {
        txtNombre.setDisable(deshabilitar);
        txtPresentacion.setDisable(deshabilitar);
        btnGuardarMedicamento.setDisable(deshabilitar);
        btnVolver.setDisable(deshabilitar);
    }

    private void mostrarCargando(boolean mostrar) {
        if (progressIndicator != null) {
            progressIndicator.setVisible(mostrar);
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInformacion(String mensaje) {
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