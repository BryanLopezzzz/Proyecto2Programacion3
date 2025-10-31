package hospital.controller.busqueda;

import hospital.controller.Alerta;
import hospital.logica.PacienteLogica;
import hospital.model.Administrador;
import hospital.model.Paciente;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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

    @FXML
    private ProgressIndicator progressIndicator;

    private final PacienteLogica pacienteIntermediaria = new PacienteLogica();
    private final Administrador admin = new Administrador();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ObservableList<Paciente> pacientesObs;
    private ObservableList<Paciente> todosPacientes;
    private Paciente pacienteSeleccionado;
    private Timer searchTimer;

    @FXML
    public void initialize() {
        configurarColumnas();
        configurarFiltro();
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }
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
        deshabilitarControles(true);
        mostrarCargando(true);

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
                    mostrarCargando(false);
                    deshabilitarControles(false);
                },
                // OnError
                error -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.error("Error","Error al cargar pacientes: " + error.getMessage());

                    pacientesObs = FXCollections.observableArrayList();
                    todosPacientes = FXCollections.observableArrayList();
                    tableMedicos.setItems(pacientesObs);
                }
        );
    }

    private void configurarBusquedaEnTiempoReal() {
        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            if (searchTimer != null) {
                searchTimer.cancel();
            }

            searchTimer = new Timer();
            searchTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> BuscarPaciente(null));
                }
            }, 300);
        });
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
            pacientesObs.setAll(todosPacientes);
            return;
        }

        String criterio = texto.toLowerCase().trim();
        String filtro = btnFiltro.getValue();

        buscarPacientesAsync(criterio, filtro);
    }

    private void buscarPacientesAsync(String criterio, String filtro) {
        txtBuscar.setDisable(true);
        btnFiltro.setDisable(true);
        mostrarCargando(true);

        Async.Run(
                () -> {
                    try {
                        List<Paciente> resultados;
                        if ("Nombre".equals(filtro)) {
                            resultados = pacienteIntermediaria.buscarPorNombre(admin, criterio);
                        } else { // ID
                            Paciente p = pacienteIntermediaria.buscarPorId(criterio);
                            resultados = (p != null) ? List.of(p) : List.of();
                        }
                        return resultados;
                    } catch (Exception e) {
                        throw new RuntimeException("Error en búsqueda: " + e.getMessage(), e);
                    }
                },
                // OnSuccess
                resultados -> {
                    pacientesObs.setAll(resultados);
                    txtBuscar.setDisable(false);
                    btnFiltro.setDisable(false);
                    mostrarCargando(false);
                },
                // OnError
                error -> {
                    txtBuscar.setDisable(false);
                    btnFiltro.setDisable(false);
                    mostrarCargando(false);
                    Alerta.error("Error","Error al buscar pacientes: " + error.getMessage());
                }
        );
    }

    @FXML
    private void Volver() {
        if (searchTimer != null) {
            searchTimer.cancel();
        }
        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void Seleccionar(ActionEvent actionEvent) {
        pacienteSeleccionado = tableMedicos.getSelectionModel().getSelectedItem();
        if (pacienteSeleccionado == null) {
            Alerta.error("Error","Debe seleccionar un paciente.");
            return;
        }
        if (searchTimer != null) {
            searchTimer.cancel();
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

    private void deshabilitarControles(boolean deshabilitar) {
        btnSeleccionar.setDisable(deshabilitar);
        txtBuscar.setDisable(deshabilitar);
        btnFiltro.setDisable(deshabilitar);
        tableMedicos.setDisable(deshabilitar);
    }

    private void mostrarCargando(boolean mostrar) {
        if (progressIndicator != null) {
            progressIndicator.setVisible(mostrar);
        }
    }
}
