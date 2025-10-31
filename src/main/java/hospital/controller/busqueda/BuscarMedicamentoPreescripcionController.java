package hospital.controller.busqueda;

import hospital.controller.Alerta;
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
        cargarMedicamentosAsync();
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

    private void cargarMedicamentosAsync() {
        Async.Run(
                () -> {
                    try {
                        return medicamentoIntermediaria.listar(administrador);
                    } catch (Exception e) {
                        throw new RuntimeException("Error al cargar medicamentos: " + e.getMessage(), e);
                    }
                },
                // OnSuccess
                lista -> {
                    medicamentos.clear();
                    medicamentos.addAll(lista);

                    todosMedicamentos.clear();
                    todosMedicamentos.addAll(lista);
                },
                // OnError
                error -> {
                    Alerta.error("Error","Error al cargar medicamentos: " + error.getMessage());
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

        String filtro = btnFiltro.getValue();
        String criterio = textoBusqueda.trim();

        if ("Código".equals(filtro) || "Nombre".equals(filtro)) {
            buscarEnBaseDatosAsync(filtro, criterio);
        } else {
            buscarLocal(filtro, criterio);
        }
    }

    private void buscarEnBaseDatosAsync(String filtro, String criterio) {
        Async.Run(
                () -> {
                    try {
                        List<Medicamento> resultados = new ArrayList<>();

                        if ("Código".equals(filtro)) {
                            Medicamento medicamento = medicamentoIntermediaria.buscarPorCodigo(administrador, criterio);
                            if (medicamento != null) {
                                resultados.add(medicamento);
                            }
                        } else if ("Nombre".equals(filtro)) {
                            resultados = medicamentoIntermediaria.buscarPorNombre(administrador, criterio);
                        }

                        return resultados;
                    } catch (Exception e) {
                        throw new RuntimeException("Error en búsqueda: " + e.getMessage(), e);
                    }
                },
                // OnSuccess
                resultados -> {
                    medicamentos.clear();
                    medicamentos.addAll(resultados);

                    if (resultados.isEmpty()) {
                        Alerta.advertencia("Advertencia","No se encontraron resultados para: " + criterio);
                    }
                },
                // OnError
                error -> {
                    Alerta.error("Error","Error al buscar medicamentos: " + error.getMessage());
                }
        );
    }

    private void buscarLocal(String filtro, String criterio) {
        List<Medicamento> resultados = new ArrayList<>();
        String busquedaMin = criterio.toLowerCase();

        switch (filtro) {
            case "Presentación":
                resultados = todosMedicamentos.stream()
                        .filter(m -> m.getPresentacion() != null &&
                                m.getPresentacion().toLowerCase().contains(busquedaMin))
                        .toList();
                break;

            case "Todos":
                resultados = todosMedicamentos.stream()
                        .filter(m ->
                                (m.getCodigo() != null && m.getCodigo().toLowerCase().contains(busquedaMin)) ||
                                        (m.getNombre() != null && m.getNombre().toLowerCase().contains(busquedaMin)) ||
                                        (m.getPresentacion() != null && m.getPresentacion().toLowerCase().contains(busquedaMin))
                        )
                        .toList();
                break;

            default:
                resultados = todosMedicamentos;
                break;
        }

        medicamentos.clear();
        medicamentos.addAll(resultados);

        if (resultados.isEmpty()) {
            Alerta.advertencia("Advertencia","No se encontraron resultados para: " + criterio);
        }
    }

    @FXML
    private void Seleccionar() {
        medicamentoSeleccionado = tblMedicamento.getSelectionModel().getSelectedItem();
        if (medicamentoSeleccionado == null) {
            Alerta.error("Error","Debe seleccionar un medicamento.");
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


    public Medicamento getMedicamentoSeleccionado() {
        return medicamentoSeleccionado;
    }
}

