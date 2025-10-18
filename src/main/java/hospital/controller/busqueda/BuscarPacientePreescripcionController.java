package hospital.controller.busqueda;

import hospital.logica.PacienteLogica;
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

    private final PacienteLogica pacienteIntermediaria = new PacienteLogica();
    private final Administrador admin = new Administrador();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ObservableList<Paciente> pacientesObs;
    private ObservableList<Paciente> todosPacientes;
    private Paciente pacienteSeleccionado;

    @FXML
    public void initialize() {
        configurarColumnas();
        configurarFiltro();
        cargarPacientesAsync();
        configurarBusquedaEnTiempoReal();
        configurarDobleClick();
    }

    private void configurarColumnas() {
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
    }

    private void configurarFiltro() {
        btnFiltro.setItems(FXCollections.observableArrayList("Nombre", "ID"));
        btnFiltro.setValue("Nombre");
    }



    private void cargarPacientesAsync() {
        Async.Run(
                () -> {
                    try {
                        return pacienteIntermediaria.listar(admin);
                    } catch (Exception e) {
                        throw new RuntimeException("Error al cargar pacientes: " + e.getMessage(), e);
                    }
                },
                // OnSuccess
                lista -> {
                    pacientesObs = FXCollections.observableArrayList(lista);
                    todosPacientes = FXCollections.observableArrayList(lista);
                    tableMedicos.setItems(pacientesObs);
                },
                // OnError
                error -> {
                    mostrarError("Error al cargar pacientes: " + error.getMessage());
                }
        );
    }

    private void configurarBusquedaEnTiempoReal() {
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> BuscarPaciente(null));
    }
//   Permitir seleccionar con doble clic
    private void configurarDobleClick() {
        tableMedicos.setRowFactory(tv -> {
            TableRow<Paciente> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Seleccionar(null);
                }
            });
            return row;
        });
    }

    @FXML
    public void BuscarPaciente(ActionEvent event) {
        String texto = txtBuscar.getText();

        if (texto == null || texto.trim().isEmpty()) {
            if (todosPacientes != null) {
                tableMedicos.setItems(todosPacientes);
            }
            return;
        }

        if (todosPacientes == null || todosPacientes.isEmpty()) {
            return;
        }

        String criterio = texto.toLowerCase().trim();
        String filtro = btnFiltro.getValue();

        List<Paciente> filtrados;
        if ("Nombre".equals(filtro)) {
            filtrados = todosPacientes.stream()
                    .filter(p -> p.getNombre() != null &&
                            p.getNombre().toLowerCase().contains(criterio))
                    .collect(Collectors.toList());
        } else { // ID
            filtrados = todosPacientes.stream()
                    .filter(p -> p.getId() != null &&
                            p.getId().toLowerCase().contains(criterio))
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
        Stage stage = (Stage) btnSeleccionar.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void Filtrar(ActionEvent event) {
        BuscarPaciente(event);
    }

    public Paciente getPacienteSeleccionado() {
        return pacienteSeleccionado;
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
