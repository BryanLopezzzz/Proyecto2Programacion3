package hospital.controller.registro;

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

public class RegistroMedicamentoController implements Initializable {

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtPresentacion;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnVolver;

    private final MedicamentoIntermediaria medicamentoIntermediaria;
    private final Administrador administrador;

    public RegistroMedicamentoController() {
        this.medicamentoIntermediaria = new MedicamentoIntermediaria();
        this.administrador = new Administrador();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    private void guardar() {
        String codigo = txtCodigo.getText();
        String nombre = txtNombre.getText();
        String presentacion = txtPresentacion.getText();

        if (codigo == null || codigo.trim().isEmpty()) {
            mostrarError("El código es obligatorio.");
            return;
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            mostrarError("El nombre es obligatorio.");
            return;
        }
        if (presentacion == null || presentacion.trim().isEmpty()) {
            mostrarError("La presentación es obligatoria.");
            return;
        }

        try {
            Medicamento medicamento = new Medicamento(
                    codigo.trim(),
                    nombre.trim(),
                    presentacion.trim()
            );

            medicamentoIntermediaria.agregar(administrador, medicamento);

            mostrarInfo("Medicamento guardado exitosamente.\n" +
                    "Medicamento: " + medicamento.getNombre() + "\n" +
                    "Codigo: " + medicamento.getCodigo() + "\n" +
                    "Presentacion: " + medicamento.getPresentacion());

            volverABusqueda();

        } catch (Exception e) {
            mostrarError("Error al agregar medicamento: " + e.getMessage());
        }
    }

    @FXML
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
    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registro Exitoso");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

}