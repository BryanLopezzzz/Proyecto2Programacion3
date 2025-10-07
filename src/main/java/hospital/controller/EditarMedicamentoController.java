package hospital.controller;

import hospital.Intermediaria.MedicamentoIntermediaria;
import hospital.model.Administrador;
import hospital.model.Medicamento;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
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

    private final MedicamentoIntermediaria medicamentoIntermediaria;
    private final Administrador administrador;
    private Medicamento medicamentoOriginal;

    public EditarMedicamentoController() {
        this.medicamentoIntermediaria = new MedicamentoIntermediaria();
        this.administrador = new Administrador();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // El código del medicamento no debería ser editable
        txtCodigo.setEditable(false);
        txtCodigo.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #b3b3b3; -fx-border-radius: 4;");
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
        String nombre = txtNombre.getText();
        String presentacion = txtPresentacion.getText();

        // Validaciones
        if (nombre == null || nombre.trim().isEmpty()) {
            mostrarError("El nombre es obligatorio.");
            return;
        }

        if (presentacion == null || presentacion.trim().isEmpty()) {
            mostrarError("La presentación es obligatoria.");
            return;
        }

        if (medicamentoOriginal == null) {
            mostrarError("Error: No se ha establecido el medicamento a editar.");
            return;
        }

        try {
            // Crear medicamento con los nuevos datos
            Medicamento medicamentoEditado = new Medicamento(
                    medicamentoOriginal.getCodigo(), // El código no cambia
                    nombre.trim(),
                    presentacion.trim()
            );

            // Actualizar el medicamento usando el controlador
            medicamentoIntermediaria.modificar(administrador, medicamentoEditado);

            // Mostrar confirmación
            mostrarInformacion("Medicamento actualizado correctamente.");

            // Cerrar ventana tras editar exitosamente
            Stage stage = (Stage) btnGuardarMedicamento.getScene().getWindow();
            Volver();

        } catch (Exception e) {
            mostrarError("Error al actualizar medicamento: " + e.getMessage());
        }
    }

    @FXML
    private void Volver() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/MedicamentosAdmin.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Buscar Medicamentos");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al volver a la vista de búsqueda.");
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
}