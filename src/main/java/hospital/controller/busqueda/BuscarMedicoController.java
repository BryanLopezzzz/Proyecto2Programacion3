package hospital.controller.busqueda;

import hospital.logica.MedicoLogica;
import hospital.model.Administrador;
import hospital.model.Medico;
import hospital.controller.EditarMedicoController;
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

public class BuscarMedicoController {

    @FXML
    private TextField txtBuscar;

    @FXML
    private ComboBox<String> cmbFiltrar;

    @FXML
    private TableView<Medico> tblMedicos;

    @FXML
    private TableColumn<Medico, String> colIdentificacion;

    @FXML
    private TableColumn<Medico, String> colNombre;

    @FXML
    private TableColumn<Medico, String> colEspecialidad;

    @FXML
    private Button btnAgregarMedico;

    @FXML
    private Button btnEliminarMedico;

    @FXML
    private Button btnEditarMedico;

    @FXML
    private Button btnVolver;

    @FXML
    private Button btnReporte;

    @FXML
    private Button btnBuscar;

    private final MedicoLogica medicoIntermediaria = new MedicoLogica();
    private final Administrador admin = new Administrador(); // puedes pasar el admin logueado

    private ObservableList<Medico> medicosObs;

    @FXML
    public void initialize() {
        // Configurar columnas
        colIdentificacion.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getId()));
        colNombre.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNombre()));
        colEspecialidad.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEspecialidad()));

        // Inicializar filtro
        cmbFiltrar.setItems(FXCollections.observableArrayList("Nombre", "ID"));
        cmbFiltrar.setValue("Nombre");

        cargarMedicos();
    }

    private void cargarMedicos() {
        try {
            List<Medico> lista = medicoIntermediaria.listar(admin);
            medicosObs = FXCollections.observableArrayList(lista);
            tblMedicos.setItems(medicosObs);
        } catch (Exception e) {
            mostrarError("Error al cargar médicos: " + e.getMessage());
        }
    }

    @FXML
    public void AgregarMedico(ActionEvent event) {
        try {
            // Usar la ventana de agregar médico
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/agregarMedico.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) btnAgregarMedico.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Agregar Médico");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al abrir la ventana de agregar médico: " + e.getMessage());
        }
    }

    @FXML
    public void EliminarMedico(ActionEvent event) {
        Medico seleccionado = tblMedicos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Debe seleccionar un médico para eliminar.");
            return;
        }

        // Confirmar eliminación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro que desea eliminar el médico?");
        confirmacion.setContentText("Médico: " + seleccionado.getNombre() +
                "\nID: " + seleccionado.getId() +
                "\nEspecialidad: " + seleccionado.getEspecialidad());

        if (confirmacion.showAndWait().get() == ButtonType.OK) {
            try {
                medicoIntermediaria.borrar(admin, seleccionado.getId());
                mostrarInfo("Médico eliminado correctamente.");
                cargarMedicos();
            } catch (Exception e) {
                mostrarError("Error al eliminar médico: " + e.getMessage());
            }
        }
    }

    @FXML
    public void EditarMedico(ActionEvent event) {
        Medico seleccionado = tblMedicos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Debe seleccionar un médico para editar.");
            return;
        }

        try {
            // Cargar la ventana de editar médico
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/editarMedico.fxml"));
            Parent root = fxmlLoader.load();

            // Obtener el controlador de la ventana de edición
            EditarMedicoController editarController = fxmlLoader.getController();

            // Pasar los datos del médico seleccionado al controlador de edición
            editarController.inicializarConMedico(seleccionado);

            Stage stage = (Stage) btnEditarMedico.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Editar Médico");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al abrir la ventana de editar médico: " + e.getMessage());
        }
    }

    @FXML
    public void GenerarReporte(ActionEvent event) {
        try {
            // Obtener todos los médicos desde la base de datos
            List<Medico> reporte = medicoIntermediaria.generarReporte(admin);

            // Actualizar la tabla si tienes TableView, por ejemplo:
            tblMedicos.getItems().setAll(reporte);

            mostrarInfo("Reporte generado correctamente desde la base de datos.");

        } catch (Exception e) {
            mostrarError("Error al generar reporte: " + e.getMessage());
        }
    }

    @FXML
    public void Buscar(ActionEvent event) {
        String criterio = txtBuscar.getText().trim();
        String filtro = cmbFiltrar.getValue();

        if (criterio.isEmpty()) {
            cargarMedicos();
            return;
        }

        try {
            List<Medico> resultados;
            if ("Nombre".equalsIgnoreCase(filtro)) {
                resultados = medicoIntermediaria.buscarPorNombre(admin, criterio);
            } else { // ID
                Medico m = medicoIntermediaria.buscarPorId(admin, criterio);
                resultados = (m != null) ? List.of(m) : List.of();
            }

            medicosObs = FXCollections.observableArrayList(resultados);
            tblMedicos.setItems(medicosObs);

            if (resultados.isEmpty()) {
                mostrarInfo("No se encontraron médicos con el criterio especificado.");
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

    // Método para filtrar (llamado por el ComboBox)
    public void filtrar(ActionEvent event) {
        Buscar(event);
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