package hospital.controller.registro;

import hospital.controller.Alerta;
import hospital.controller.busqueda.Async;
import hospital.logica.MedicamentoLogica;
import hospital.model.Administrador;
import hospital.model.Medicamento;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
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

    @FXML
    private ProgressIndicator progressIndicator;

    private final MedicamentoLogica medicamentoIntermediaria;
    private final Administrador administrador;

    public RegistroMedicamentoController() {
        this.medicamentoIntermediaria = new MedicamentoLogica();
        this.administrador = new Administrador();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }
    }

    @FXML
    private void guardar() {
        String codigo = txtCodigo.getText();
        String nombre = txtNombre.getText();
        String presentacion = txtPresentacion.getText();

        if (codigo == null || codigo.trim().isEmpty()) {
            Alerta.error("Error","El código es obligatorio.");
            return;
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            Alerta.error("Error","El nombre es obligatorio.");
            return;
        }
        if (presentacion == null || presentacion.trim().isEmpty()) {
            Alerta.error("Error","La presentación es obligatoria.");
            return;
        }
        guardarAsync(codigo.trim(), nombre.trim(), presentacion.trim());
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
            Alerta.error("Error","Error al volver a la vista de búsqueda.");
        }
    }
    private void guardarAsync(String codigo, String nombre, String presentacion) {
        deshabilitarControles(true);
        mostrarCargando(true);

        Async.Run(
                () -> {
                    try {
                        Medicamento medicamento = new Medicamento(codigo, nombre, presentacion);
                        medicamentoIntermediaria.agregar(administrador, medicamento);
                        return medicamento;
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                },

                // OnSuccess
                medicamento -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);

                    Alerta.info("Información","Medicamento guardado exitosamente.\n" +
                            "Medicamento: " + medicamento.getNombre() + "\n" +
                            "Codigo: " + medicamento.getCodigo() + "\n" +
                            "Presentacion: " + medicamento.getPresentacion());

                    limpiarCampos();
                    volverABusqueda();
                },

                // OnError
                error -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.error("Error","Error al agregar medicamento: " + error.getMessage());
                }
        );
    }

    private void limpiarCampos() {
        txtCodigo.clear();
        txtNombre.clear();
        txtPresentacion.clear();
        txtCodigo.requestFocus();
    }

    private void deshabilitarControles(boolean deshabilitar) {
        txtCodigo.setDisable(deshabilitar);
        txtNombre.setDisable(deshabilitar);
        txtPresentacion.setDisable(deshabilitar);
        btnGuardar.setDisable(deshabilitar);
        btnVolver.setDisable(deshabilitar);
    }

    private void mostrarCargando(boolean mostrar) {
        if (progressIndicator != null) {
            progressIndicator.setVisible(mostrar);
        }
    }

}