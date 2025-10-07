package hospital.controller.busqueda;

import hospital.Intermediaria.MedicamentoIntermediaria;
import hospital.model.Administrador;
import hospital.model.Medicamento;
import hospital.controller.EditarMedicamentoController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BuscarMedicamentoController implements Initializable {

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> btnFiltro;
    @FXML private TableView<Medicamento> tblMedicos;
    @FXML private TableColumn<Medicamento, String> colCodigo;
    @FXML private TableColumn<Medicamento, String> colNombre;
    @FXML private TableColumn<Medicamento, String> colPresentacion;
    @FXML private Button btnBuscar;
    @FXML private Button btnAgregarMedicamento;
    @FXML private Button btnEliminar;
    @FXML private Button btnEditarMedicamento;
    @FXML private Button btnVolver;
    @FXML private Button btnReporte;

    private final MedicamentoIntermediaria medicamentoIntermediaria;
    private final Administrador administrador;
    private ObservableList<Medicamento> medicamentos;
    private Medicamento medicamentoSeleccionado;


    public BuscarMedicamentoController() {
        this.medicamentoIntermediaria = new MedicamentoIntermediaria();
        this.administrador = new Administrador();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        configurarFiltro();
        cargarMedicamentos();
    }

    private void configurarTabla() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPresentacion.setCellValueFactory(new PropertyValueFactory<>("presentacion"));

        medicamentos = FXCollections.observableArrayList();
        tblMedicos.setItems(medicamentos);
    }

    private void configurarFiltro() {
        btnFiltro.getItems().addAll("ID", "Nombre");
        btnFiltro.setValue("ID");
    }

    private void cargarMedicamentos() {
        try {
            List<Medicamento> lista = medicamentoIntermediaria.listar(administrador);
            medicamentos.clear();
            medicamentos.addAll(lista);
        } catch (Exception e) {
            mostrarError("Error al cargar medicamentos: " + e.getMessage());
        }
    }

    @FXML
    private void Buscar() {
        String textoBusqueda = txtBuscar.getText();
        if (textoBusqueda == null || textoBusqueda.trim().isEmpty()) {
            cargarMedicamentos();
            return;
        }

        try {
            String filtro = btnFiltro.getValue();
            List<Medicamento> resultados;

            if ("ID".equals(filtro)) {
                Medicamento medicamento = medicamentoIntermediaria.buscarPorCodigo(administrador, textoBusqueda.trim());
                if (medicamento != null) {
                    resultados = List.of(medicamento);
                } else {
                    resultados = List.of();
                }
            } else {
                resultados = medicamentoIntermediaria.buscarPorNombre(administrador, textoBusqueda.trim());
            }

            medicamentos.clear();
            medicamentos.addAll(resultados);

        } catch (Exception e) {
            mostrarError("Error al buscar medicamentos: " + e.getMessage());
        }
    }

    @FXML
    private void AgregarMedicamento() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/AgregarMedicamento.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) btnAgregarMedicamento.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Agregar Medicamento");
            stage.show();

        } catch (Exception e) {
            mostrarError("Error al abrir ventana agregar medicamento: " + e.getMessage());
        }
    }
    @FXML
    private void EliminarMedicamento() {
        Medicamento seleccionado = tblMedicos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Debe seleccionar un medicamento para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Está seguro de eliminar el medicamento: " + seleccionado.getNombre() + "?");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                medicamentoIntermediaria.borrar(administrador, seleccionado.getCodigo());
                cargarMedicamentos();
            } catch (Exception e) {
                mostrarError("Error al eliminar medicamento: " + e.getMessage());
            }
        }
    }

    @FXML
    private void EditarMedicamento() {
        Medicamento seleccionado = tblMedicos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Debe seleccionar un medicamento para editar.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/editarMedicamento.fxml"));
            Parent root = loader.load();

            EditarMedicamentoController editarController = loader.getController();
            editarController.setMedicamento(seleccionado);

            Stage stage = (Stage) btnEditarMedicamento.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Editar Medicamento");
            stage.show();

        } catch (Exception e) {
            mostrarError("Error al abrir ventana editar medicamento: " + e.getMessage());
        }
    }
    @FXML
    private void Volver() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/dashboard.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error al cargar el dashboard.");
            alert.showAndWait();
        }
    }

    @FXML
    private void GenerarReporte() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte de Medicamentos");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos XML", "*.xml")
        );
        fileChooser.setInitialFileName("reporte_medicamentos.xml");

        Stage stage = (Stage) btnReporte.getScene().getWindow();
        File archivo = fileChooser.showSaveDialog(stage);

        if (archivo != null) {
            try {
                medicamentoIntermediaria.generarReporte(administrador, archivo.getAbsolutePath());
            } catch (Exception e) {
                mostrarError("Error al generar reporte: " + e.getMessage());
            }
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public Medicamento getMedicamentoSeleccionado() {
        return medicamentoSeleccionado;
    }

    @FXML
    private void SeleccionarMedicamento() {
        medicamentoSeleccionado = tblMedicos.getSelectionModel().getSelectedItem();
        if (medicamentoSeleccionado == null) {
            mostrarError("Debe seleccionar un medicamento.");
            return;
        }
        Stage stage = (Stage) tblMedicos.getScene().getWindow();
        stage.close();
    }

}
