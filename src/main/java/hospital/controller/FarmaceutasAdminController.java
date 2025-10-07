package hospital.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class FarmaceutasAdminController {
    @FXML
    private Button btnVolver;
    @FXML
    private ComboBox<String> btnFiltrar;
    @FXML
    private TextField txtBuscar;
    @FXML
    private Button btnAgregarFarmaceuta;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnReporte;
    @FXML
    private Button btnBuscar;

    @FXML
    public void Filtrar(ActionEvent event) {

    }

    @FXML
    public void AgregarFarmaceuta(ActionEvent event) {

    }

    @FXML
    public void EliminarFarmaceuta(ActionEvent event) {

    }

    @FXML
    public void EditarFarmaceuta(ActionEvent event) {

    }

    @FXML
    public void GenerarReporte(ActionEvent event) {

    }

    @FXML
    public void Buscar(ActionEvent event) {

    }

    @FXML
    public void Volver () {
        try {
            FXMLLoader FXMLLoader = new FXMLLoader(getClass().getResource("/hospital/view/dashboard.fxml"));
            Parent root = FXMLLoader.load();
            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Administración Farmaceutas");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error al cargar el Administración Farmaceutas.");
            alert.showAndWait();
        }
    }
}
