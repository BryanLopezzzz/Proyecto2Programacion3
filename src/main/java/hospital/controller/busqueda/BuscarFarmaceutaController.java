package hospital.controller.busqueda;

import hospital.controller.Alerta;
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
import javafx.stage.Stage;

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

    @FXML
    private ProgressIndicator progressIndicator;

    private final FarmaceutaLogica farmaceutaIntermediaria = new FarmaceutaLogica();
    private final Administrador admin = new Administrador();

    private ObservableList<Farmaceuta> farmaceutasObs;

    @FXML
    public void initialize() {
        colIdentificacion.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getId()));
        colNombre.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNombre()));

        btnFiltrar.setItems(FXCollections.observableArrayList("ID", "Nombre"));
        btnFiltrar.setValue("ID");

        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }
        cargarFarmaceutasAsync();
    }

    private void cargarFarmaceutasAsync() {
        deshabilitarControles(true);
        mostrarCargando(true);

        Async.Run(
                () -> {
                    try {
                        return farmaceutaIntermediaria.listar(admin);
                    } catch (Exception e) {
                        throw new RuntimeException("Error al cargar farmaceutas: " + e.getMessage(), e);
                    }
                },
                // OnSuccess
                lista -> {
                    farmaceutasObs = FXCollections.observableArrayList(lista);
                    tblFarmaceutas.setItems(farmaceutasObs);
                    mostrarCargando(false);
                    deshabilitarControles(false);
                },
                // OnError
                error -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.error("Error", "Error al cargar farmaceutas: " + error.getMessage());
                }
        );
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
            Alerta.error("Error","Error al abrir la ventana de agregar farmaceuta: " + e.getMessage());
        }
    }

    @FXML
    public void EliminarFarmaceuta(ActionEvent event) {
        Farmaceuta seleccionado = tblFarmaceutas.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alerta.error("Error","Debe seleccionar un farmaceuta para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro que desea eliminar el farmaceuta?");
        confirmacion.setContentText("Farmaceuta: " + seleccionado.getNombre() + " (ID: " + seleccionado.getId() + ")");

        if (confirmacion.showAndWait().get() == ButtonType.OK) {
            eliminarFarmaceutaAsync(seleccionado);
        }
    }

    private void eliminarFarmaceutaAsync(Farmaceuta farmaceuta) {
        deshabilitarControles(true);
        mostrarCargando(true);

        Async.runVoid(
                () -> {
                    try {
                        farmaceutaIntermediaria.borrar(admin, farmaceuta.getId());
                    } catch (Exception e) {
                        throw new RuntimeException("Error al eliminar: " + e.getMessage(), e);
                    }
                },
                // OnSuccess
                () -> {
                    farmaceutasObs.remove(farmaceuta);
                    tblFarmaceutas.refresh();
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.info("Éxito", "Farmaceuta eliminado correctamente.");
                },
                // OnError
                error -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.error("Error", "Error al eliminar farmaceuta: " + error.getMessage());
                }
        );
    }

    @FXML
    public void EditarFarmaceuta(ActionEvent event) {
        Farmaceuta seleccionado = tblFarmaceutas.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alerta.error("Error","Debe seleccionar un farmaceuta para editar.");
            return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/editarFarmaceuta.fxml"));
            Parent root = fxmlLoader.load();

            EditarFarmaceutaController editarController = fxmlLoader.getController();
            editarController.cargarFarmaceuta(seleccionado);

            Stage stage = (Stage) btnEditar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Editar Farmaceuta");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alerta.error("Error","Error al abrir la ventana de edición: " + e.getMessage());
        }
    }

    @FXML
    public void GenerarReporte(ActionEvent event) {
        generarReporteAsync();
    }

    private void generarReporteAsync() {
        deshabilitarControles(true);
        mostrarCargando(true);

        Async.Run(
                () -> {
                    try {
                        return farmaceutaIntermediaria.generarReporte(admin);
                    } catch (Exception e) {
                        throw new RuntimeException("Error al generar reporte: " + e.getMessage(), e);
                    }
                },
                // OnSuccess
                reporte -> {
                    tblFarmaceutas.getItems().setAll(reporte);
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.info("Éxito", "Reporte generado correctamente.");
                },
                // OnError
                error -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.error("Error", "Error al generar reporte: " + error.getMessage());
                }
        );
    }

    @FXML
    public void Buscar(ActionEvent event) {
        String criterio = txtBuscar.getText().trim();
        String filtro = btnFiltrar.getValue();

        if (criterio.isEmpty()) {
            cargarFarmaceutasAsync();
            return;
        }

        buscarFarmaceutasAsync(criterio, filtro);
    }

    private void buscarFarmaceutasAsync(String criterio, String filtro) {
        deshabilitarControles(true);
        mostrarCargando(true);

        Async.Run(
                () -> {
                    try {
                        List<Farmaceuta> resultados;
                        if ("Nombre".equalsIgnoreCase(filtro)) {
                            resultados = farmaceutaIntermediaria.buscarPorNombre(admin, criterio);
                        } else { // ID
                            Farmaceuta f = farmaceutaIntermediaria.buscarPorId(admin, criterio);
                            resultados = (f != null) ? List.of(f) : List.of();
                        }
                        return resultados;
                    } catch (Exception e) {
                        throw new RuntimeException("Error en búsqueda: " + e.getMessage(), e);
                    }
                },
                // OnSuccess
                resultados -> {
                    farmaceutasObs = FXCollections.observableArrayList(resultados);
                    tblFarmaceutas.setItems(farmaceutasObs);
                    mostrarCargando(false);
                    deshabilitarControles(false);

                    if (resultados.isEmpty()) {
                        Alerta.info("Búsqueda", "No se encontraron farmaceutas con el criterio especificado.");
                    }
                },
                // OnError
                error -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.error("Error", "Error en búsqueda: " + error.getMessage());
                }
        );
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
            Alerta.error("Error","Error al cargar el dashboard.");
        }
    }

    private void deshabilitarControles(boolean deshabilitar) {
        btnAgregarFarmaceuta.setDisable(deshabilitar);
        btnEliminar.setDisable(deshabilitar);
        btnEditar.setDisable(deshabilitar);
        btnBuscar.setDisable(deshabilitar);
        btnReporte.setDisable(deshabilitar);
        txtBuscar.setDisable(deshabilitar);
        btnFiltrar.setDisable(deshabilitar);
    }

    private void mostrarCargando(boolean mostrar) {
        if (progressIndicator != null) {
            progressIndicator.setVisible(mostrar);
        }
    }
}