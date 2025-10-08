package hospital.controller.busqueda;

import hospital.logica.MedicamentoLogica;
import hospital.model.Administrador;
import hospital.model.Medicamento;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;

public class BuscarMedicamentoPreescripcionController implements Initializable {

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> btnFiltro;
    @FXML private TableView<Medicamento> tblMedicamento;
    @FXML private TableColumn<Medicamento, String> colCodigo;
    @FXML private TableColumn<Medicamento, String> colNombreMedicamento;
    @FXML private TableColumn<Medicamento, String> colPresentacion;
    @FXML private Button btnVolver;
    @FXML private Button btnSeleccionar;

    private final MedicamentoLogica medicamentoIntermediaria;
    private final Administrador administrador;
    private ObservableList<Medicamento> medicamentos;
    private ObservableList<Medicamento> todosMedicamentos;
    private Medicamento medicamentoSeleccionado;
    private Timer searchTimer;

    public BuscarMedicamentoPreescripcionController() {
        this.medicamentoIntermediaria = new MedicamentoLogica();
        this.administrador = new Administrador();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        configurarFiltro();
        cargarMedicamentos();
        configurarBusquedaEnTiempoReal();
    }

    private void configurarTabla() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombreMedicamento.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPresentacion.setCellValueFactory(new PropertyValueFactory<>("presentacion"));

        medicamentos = FXCollections.observableArrayList();
        todosMedicamentos = FXCollections.observableArrayList();
        tblMedicamento.setItems(medicamentos);

        // Permitir selección con doble clic
        tblMedicamento.setRowFactory(tv -> {
            TableRow<Medicamento> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Seleccionar();
                }
            });
            return row;
        });
    }

    private void configurarFiltro() {
        btnFiltro.getItems().addAll("Código", "Nombre");
        btnFiltro.setValue("Nombre");
    }

    private void cargarMedicamentos() {
        try {
            List<Medicamento> lista = medicamentoIntermediaria.listar(administrador);
            medicamentos.clear();
            medicamentos.addAll(lista);

            todosMedicamentos.clear();
            todosMedicamentos.addAll(lista);
        } catch (Exception e) {
            mostrarError("Error al cargar medicamentos: " + e.getMessage());
        }
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
                    Platform.runLater(() -> Buscar());
                }
            }, 300); // Buscar después de 300ms de no escribir
        });
    }

    @FXML
    private void Filtrar() {
        Buscar();
    }

    @FXML
    private void Buscar() {
        String textoBusqueda = txtBuscar.getText();
        if (textoBusqueda == null || textoBusqueda.trim().isEmpty()) {
            medicamentos.clear();
            medicamentos.addAll(todosMedicamentos);
            return;
        }

        try {
            String filtro = btnFiltro.getValue();
            List<Medicamento> resultados = new ArrayList<>();

            switch (filtro) {
                case "Código":
                    // Búsqueda exacta por código
                    Medicamento medicamento = medicamentoIntermediaria.buscarPorCodigo(administrador, textoBusqueda.trim());
                    if (medicamento != null) {
                        resultados.add(medicamento);
                    }
                    break;

                case "Nombre":
                    // Búsqueda por nombre usando el controlador
                    resultados = medicamentoIntermediaria.buscarPorNombre(administrador, textoBusqueda.trim());
                    break;

                case "Presentación":
                    // Búsqueda local por presentación
                    String busquedaMin = textoBusqueda.toLowerCase().trim();
                    resultados = todosMedicamentos.stream()
                            .filter(m -> m.getPresentacion() != null &&
                                    m.getPresentacion().toLowerCase().contains(busquedaMin))
                            .toList();
                    break;

                case "Todos":
                    // Búsqueda en todos los campos
                    String busquedaTodos = textoBusqueda.toLowerCase().trim();
                    resultados = todosMedicamentos.stream()
                            .filter(m ->
                                    (m.getCodigo() != null && m.getCodigo().toLowerCase().contains(busquedaTodos)) ||
                                            (m.getNombre() != null && m.getNombre().toLowerCase().contains(busquedaTodos)) ||
                                            (m.getPresentacion() != null && m.getPresentacion().toLowerCase().contains(busquedaTodos))
                            )
                            .toList();
                    break;

                default:
                    resultados = medicamentoIntermediaria.buscarPorNombre(administrador, textoBusqueda.trim());
                    break;
            }

            medicamentos.clear();
            medicamentos.addAll(resultados);

            // Mostrar mensaje si no se encontraron resultados
            if (resultados.isEmpty()) {
                // No mostrar alert, solo dejar la tabla vacía
                System.out.println("No se encontraron resultados para: " + textoBusqueda);
            }

        } catch (Exception e) {
            mostrarError("Error al buscar medicamentos: " + e.getMessage());
        }
    }

    @FXML
    private void Seleccionar() {
        medicamentoSeleccionado = tblMedicamento.getSelectionModel().getSelectedItem();
        if (medicamentoSeleccionado == null) {
            mostrarError("Debe seleccionar un medicamento.");
            return;
        }
        Stage stage = (Stage) tblMedicamento.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void Volver() {
        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.close();
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
}

