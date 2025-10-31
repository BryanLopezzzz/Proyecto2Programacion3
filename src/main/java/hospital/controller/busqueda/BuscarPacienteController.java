package hospital.controller.busqueda;

import hospital.controller.Alerta;
import hospital.controller.EditarPacienteController;
import hospital.logica.PacienteLogica;
import hospital.model.Administrador;
import hospital.model.Paciente;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class BuscarPacienteController {

    @FXML
    private TextField txtBuscar;

    @FXML
    private ComboBox<String> btnFiltro;

    @FXML
    private TableView<Paciente> tblPacientes;

    @FXML
    private TableColumn<Paciente, String> colIdentificacionPaciente;

    @FXML
    private TableColumn<Paciente, String> colNombrePaciente;

    @FXML
    private TableColumn<Paciente, String> colTelefonoPaciente;

    @FXML
    private TableColumn<Paciente, String> colFechaNac;

    @FXML
    private Button btnAgregarPaciente;

    @FXML
    private Button btnEliminarPaciente;

    @FXML
    private Button btnEditarPaciente;

    @FXML
    private Button btnVolver;

    @FXML
    private Button btnReporte;

    @FXML
    private Button btnBuscar;

    @FXML
    private ProgressIndicator progressIndicator; // Lo nuevo por si no sirve el proyecto, tema de hilos

    private final PacienteLogica pacienteIntermediaria = new PacienteLogica();
    private final Administrador admin = new Administrador(); // puedes pasar el admin logueado
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ObservableList<Paciente> pacientesObs;

    @FXML
    public void initialize() {
        colIdentificacionPaciente.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getId()));
        colNombrePaciente.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNombre()));
        colTelefonoPaciente.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTelefono()));
        colFechaNac.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFechaNacimiento().format(formatter)
        ));

        btnFiltro.setItems(FXCollections.observableArrayList("Nombre", "ID"));
        btnFiltro.setValue("Nombre");

        cargarPacientesAsync();
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
                // OnSuccess - Se ejecuta en el hilo de JavaFX
                lista -> {
                    pacientesObs = FXCollections.observableArrayList(lista);
                    tblPacientes.setItems(pacientesObs);
                    mostrarCargando(false);
                    deshabilitarControles(false);
                },
                // OnError
                error -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.error("Error","Error al cargar pacientes: " + error.getMessage());
                }
        );
    }

    @FXML
    public void Filtrar(ActionEvent event) {
        BuscarPaciente(event);
    }

    @FXML
    public void AgregarPaciente(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/agregarPaciente.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) btnAgregarPaciente.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Agregar Paciente");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alerta.error("Error","Error al abrir la ventana de agregar paciente: " + e.getMessage());
        }
    }

    @FXML
    public void EliminarPaciente(ActionEvent event) {
        Paciente seleccionado = tblPacientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alerta.error("Error","Debe seleccionar un paciente para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro que desea eliminar el paciente?");
        confirmacion.setContentText("Paciente: " + seleccionado.getNombre() + " (ID: " + seleccionado.getId() + ")");

        if (confirmacion.showAndWait().get() == ButtonType.OK) {
            eliminarPacienteAsync(seleccionado);
        }
    }

    private void eliminarPacienteAsync(Paciente paciente) {
        deshabilitarControles(true);
        mostrarCargando(true);

        Async.runVoid(
                () -> {
                    try {
                        pacienteIntermediaria.eliminar(admin, paciente.getId());
                    } catch (Exception e) {
                        throw new RuntimeException("Error al eliminar: " + e.getMessage(), e);
                    }
                },
                // OnSuccess
                () -> {
                    pacientesObs.remove(paciente);
                    tblPacientes.refresh();
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.info("Información","Paciente eliminado correctamente.");
                },
                // OnError
                error -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.error("Error","Error al eliminar: " + error.getMessage());
                }
        );
    }
    @FXML
    public void EditarPaciente(ActionEvent event) {
        Paciente seleccionado = tblPacientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alerta.error("Error","Debe seleccionar un paciente para editar.");
            return;
        }

        try {
            // Cargar la vista de edición
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/editarPaciente.fxml"));
            Parent root = fxmlLoader.load();

            // Obtener el controller de la vista de edición y pasarle el paciente
            EditarPacienteController editarController = fxmlLoader.getController();
            editarController.cargarPaciente(seleccionado);

            // Cambiar la escena
            Stage stage = (Stage) btnEditarPaciente.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Editar Paciente");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alerta.error("Error","Error al abrir la ventana de edición: " + e.getMessage());
        }
    }

    @FXML
    public void GenerarReporte() {
        generarReporteAsync();
    }

    private void generarReporteAsync() {
        deshabilitarControles(true);
        mostrarCargando(true);

        Async.Run(
                () -> {
                    try {
                        return pacienteIntermediaria.generarReporte(admin);
                    } catch (Exception e) {
                        throw new RuntimeException("Error al generar reporte: " + e.getMessage(), e);
                    }
                },
                // OnSuccess
                reporte -> {
                    tblPacientes.getItems().setAll(reporte);
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.info("Información","Reporte generado correctamente desde la base de datos.");
                },
                // OnError
                error -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.error("Error","Error al generar reporte: " + error.getMessage());
                }
        );
    }


    @FXML
    public void BuscarPaciente(ActionEvent event) {
        String texto = txtBuscar.getText().trim();
        String filtro = btnFiltro.getValue();

        if (texto.isEmpty()) {
            cargarPacientesAsync();
            return;
        }

        buscarPacientesAsync(texto, filtro);
    }

    private void buscarPacientesAsync(String criterio, String filtro) {
        deshabilitarControles(true);
        mostrarCargando(true);

        Async.Run(
                () -> {
                    try {
                        List<Paciente> resultados;
                        if ("Nombre".equals(filtro)) {
                            resultados = pacienteIntermediaria.buscarPorNombre(admin, criterio);
                        } else {
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
                    pacientesObs = FXCollections.observableArrayList(resultados);
                    tblPacientes.setItems(pacientesObs);
                    mostrarCargando(false);
                    deshabilitarControles(false);

                    if (resultados.isEmpty()) {
                        Alerta.info("Información","No se encontraron pacientes con el criterio especificado.");
                    }
                },
                // OnError
                error -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.error("Error","Error en búsqueda: " + error.getMessage());
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
        btnAgregarPaciente.setDisable(deshabilitar);
        btnEliminarPaciente.setDisable(deshabilitar);
        btnEditarPaciente.setDisable(deshabilitar);
        btnBuscar.setDisable(deshabilitar);
        btnReporte.setDisable(deshabilitar);
        txtBuscar.setDisable(deshabilitar);
        btnFiltro.setDisable(deshabilitar);
    }

    private void mostrarCargando(boolean mostrar) {
        if (progressIndicator != null) {
            progressIndicator.setVisible(mostrar);
        }
    }
}
