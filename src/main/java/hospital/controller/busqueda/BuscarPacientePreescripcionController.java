package hospital.controller.busqueda;

import hospital.Intermediaria.PacienteIntermediaria;
import hospital.model.Administrador;
import hospital.model.Paciente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class BuscarPacientePreescripcionController {

    @FXML
    private TextField txtBuscar;

    @FXML
    private ComboBox<String> btnFiltro;

    @FXML
    private TableView<Paciente> tableMedicos;

    @FXML
    private TableColumn<Paciente, String> colIdentificacion;

    @FXML
    private TableColumn<Paciente, String> colNombre;

    @FXML
    private TableColumn<Paciente, String> colTelefono;

    @FXML
    private TableColumn<Paciente, String> colFechaNacimiento;

    @FXML
    private Button btnSeleccionar;

    @FXML
    private Button btnVolver;

    private final PacienteIntermediaria pacienteIntermediaria = new PacienteIntermediaria();
    private final Administrador admin = new Administrador();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ObservableList<Paciente> pacientesObs;
    private Paciente pacienteSeleccionado;

    @FXML
    public void initialize() {
        // Configurar columnas
        colIdentificacion.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getId()));
        colNombre.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNombre()));
        colTelefono.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTelefono()));
        colFechaNacimiento.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getFechaNacimiento().format(formatter)
                ));

        // Inicializar filtro
        btnFiltro.setItems(FXCollections.observableArrayList("Nombre", "ID"));
        btnFiltro.setValue("Nombre");

        cargarPacientes();
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> BuscarPaciente(null));
    }

    private void cargarPacientes() {
        try {
            List<Paciente> lista = pacienteIntermediaria.listar(admin);
            pacientesObs = FXCollections.observableArrayList(lista);
            tableMedicos.setItems(pacientesObs);
        } catch (Exception e) {
            mostrarError("Error al cargar pacientes: " + e.getMessage());
        }
    }

    @FXML
    public void BuscarPaciente(ActionEvent event) {
        String texto = txtBuscar.getText().toLowerCase();
        String filtro = btnFiltro.getValue();

        if (texto.isEmpty()) {
            tableMedicos.setItems(pacientesObs); // muestra todo si no hay filtro
            return;
        }

        List<Paciente> filtrados;
        if ("Nombre".equals(filtro)) {
            filtrados = pacientesObs.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains(texto))
                    .collect(Collectors.toList());
        } else { // ID
            filtrados = pacientesObs.stream()
                    .filter(p -> p.getId().toLowerCase().contains(texto))
                    .collect(Collectors.toList());
        }

        tableMedicos.setItems(FXCollections.observableArrayList(filtrados));
    }

    @FXML
    private void Volver() {
        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void Seleccionar(ActionEvent actionEvent) {
        pacienteSeleccionado = tableMedicos.getSelectionModel().getSelectedItem();
        if (pacienteSeleccionado == null) {
            mostrarError("Debe seleccionar un paciente.");
            return;
        }
        // Cierra la ventana actual
        btnSeleccionar.getScene().getWindow().hide();
    }

    @FXML
    public void Filtrar(ActionEvent event) {
        BuscarPaciente(event);
    }

    public Paciente getPacienteSeleccionado() {
        return pacienteSeleccionado;
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR, mensaje);
        alert.showAndWait();
    }
}
