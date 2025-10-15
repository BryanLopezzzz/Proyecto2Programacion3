package hospital.controller.busqueda;


import hospital.controller.EditarFarmaceutaController;
import hospital.logica.FarmaceutaLogica;
import hospital.model.Administrador;
import hospital.model.Farmaceuta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class BuscarFarmaceutaController {

    @FXML
    private TextField txtBuscar;

    @FXML
    private ComboBox<String> btnFiltrar;

    @FXML
    private TableView<Farmaceuta> tblFarmaceutas;

    @FXML
    private TableColumn<Farmaceuta, String> colIdentificacion;

    @FXML
    private TableColumn<Farmaceuta, String> colNombre;

    @FXML
    private Button btnAgregarFarmaceuta;

    @FXML
    private Button btnEliminar;

    @FXML
    private Button btnEditar;

    @FXML
    private Button btnVolver;

    @FXML
    private Button btnReporte;

    @FXML
    private Button btnBuscar;

    private final FarmaceutaLogica farmaceutaIntermediaria = new FarmaceutaLogica();
    private final Administrador admin = new Administrador(); // puedes pasar el admin logueado

    private ObservableList<Farmaceuta> farmaceutasObs;

    @FXML
    public void initialize() {
        // Configurar columnas
        colIdentificacion.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getId()));
        colNombre.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNombre()));

        // Inicializar filtro
        btnFiltrar.setItems(FXCollections.observableArrayList("ID", "Nombre"));
        btnFiltrar.setValue("ID");

        cargarFarmaceutas();
    }

    private void cargarFarmaceutas() {
        try {
            List<Farmaceuta> lista = farmaceutaIntermediaria.listar(admin);
            farmaceutasObs = FXCollections.observableArrayList(lista);
            tblFarmaceutas.setItems(farmaceutasObs);
        } catch (Exception e) {
            mostrarError("Error al cargar farmaceutas: " + e.getMessage());
        }
    }

    @FXML
    public void Filtrar(ActionEvent event) {
        Buscar(event);
    }

    @FXML
    public void AgregarFarmaceuta(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/agregarFarmaceuta.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) btnAgregarFarmaceuta.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Agregar Farmaceuta");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al abrir la ventana de agregar farmaceuta: " + e.getMessage());
        }
    }

    @FXML
    public void EliminarFarmaceuta(ActionEvent event) {
        Farmaceuta seleccionado = tblFarmaceutas.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Debe seleccionar un farmaceuta para eliminar.");
            return;
        }

        // Confirmar eliminación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro que desea eliminar el farmaceuta?");
        confirmacion.setContentText("Farmaceuta: " + seleccionado.getNombre() + " (ID: " + seleccionado.getId() + ")");

        if (confirmacion.showAndWait().get() == ButtonType.OK) {
            try {
                farmaceutaIntermediaria.borrar(admin, seleccionado.getId());
                mostrarInfo("Farmaceuta eliminado correctamente.");
                cargarFarmaceutas();
            } catch (Exception e) {
                mostrarError("Error al eliminar farmaceuta: " + e.getMessage());
            }
        }
    }

    @FXML
    public void EditarFarmaceuta(ActionEvent event) {
        Farmaceuta seleccionado = tblFarmaceutas.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Debe seleccionar un farmaceuta para editar.");
            return;
        }

        try {
            // Cargar la vista de edición
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/editarFarmaceuta.fxml"));
            Parent root = fxmlLoader.load();

            // Obtener el controller de la vista de edición y pasarle el paciente
            EditarFarmaceutaController editarController = fxmlLoader.getController();
            editarController.cargarFarmaceuta(seleccionado);

            // Cambiar la escena
            Stage stage = (Stage) btnEditar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Editar Farmaceuta");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al abrir la ventana de edición: " + e.getMessage());
        }
    }

    @FXML
    public void GenerarReporte(ActionEvent event) {
        try {
            List<Farmaceuta> reporte = farmaceutaIntermediaria.generarReporte(admin);

            tblFarmaceutas.getItems().setAll(reporte);

            mostrarInfo("Reporte generado correctamente.");

        } catch (Exception e) {
            mostrarError("Error al generar reporte: " + e.getMessage());
        }
    }

    @FXML
    public void Buscar(ActionEvent event) {
        String criterio = txtBuscar.getText().trim();
        String filtro = btnFiltrar.getValue();

        if (criterio.isEmpty()) {
            cargarFarmaceutas();
            return;
        }

        try {
            List<Farmaceuta> resultados;
            if ("Nombre".equalsIgnoreCase(filtro)) {
                resultados = farmaceutaIntermediaria.buscarPorNombre(admin, criterio);
            } else { // ID
                Farmaceuta f = farmaceutaIntermediaria.buscarPorId(admin, criterio);
                resultados = (f != null) ? List.of(f) : List.of();
            }

            farmaceutasObs = FXCollections.observableArrayList(resultados);
            tblFarmaceutas.setItems(farmaceutasObs);

            if (resultados.isEmpty()) {
                mostrarInfo("No se encontraron farmaceutas con el criterio especificado.");
            }

        } catch (Exception e) {
            mostrarError("Error en búsqueda: " + e.getMessage());
        }
    }

    @FXML
    private void Volver(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/dashboard.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al cargar el dashboard.");
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
}