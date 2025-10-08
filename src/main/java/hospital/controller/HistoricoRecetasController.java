package hospital.controller;

import hospital.logica.HistoricoRecetasLogica;
import hospital.logica.Sesion;
import hospital.model.EstadoReceta;
import hospital.model.Receta;
import hospital.model.Usuario;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HistoricoRecetasController {

    @FXML
    private TableView<Receta> tblRecetas;
    @FXML
    private TableColumn<Receta, String> colIdentificacionReceta;
    @FXML
    private TableColumn<Receta, String> colNombreMedicamento;
    @FXML
    private TableColumn<Receta, String> colPresentacion;
    @FXML
    private TableColumn<Receta, String> colFechaConfeccion;
    @FXML
    private TableColumn<Receta, String> colEstado;
    @FXML
    private Button btnVerDetalle;
    @FXML
    private Button btnVolver;
    @FXML
    private TextField txtBuscar;
    @FXML
    private ComboBox<String> cmbFiltrar;

    private HistoricoRecetasLogica controller = new HistoricoRecetasLogica();
    private ObservableList<Receta> recetasObservable;
    private ObservableList<Receta> todasLasRecetas;
    private Timer searchTimer;

    @FXML
    public void initialize() {
        colIdentificacionReceta.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombreMedicamento.setCellValueFactory(new PropertyValueFactory<>("primerMedicamento"));
        colPresentacion.setCellValueFactory(new PropertyValueFactory<>("presentacionPrimerMedicamento"));
        colFechaConfeccion.setCellValueFactory(new PropertyValueFactory<>("fechaConfeccion"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        cmbFiltrar.setItems(FXCollections.observableArrayList(
                "ID Receta", "Paciente", "Médico", "Medicamento", "Estado", "Todos"
        ));
        Usuario usuarioActual = Sesion.getUsuario();
        cargarRecetas(controller.listarRecetas(usuarioActual));

        configurarBusquedaEnTiempoReal();
        cmbFiltrar.setOnAction(event -> Buscar());
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
    private void VerDetalle() {
        Receta seleccionada = tblRecetas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            Alerta.info("Info","Debe seleccionar una receta para ver el detalle.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/verRecetaDespacho.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Detalle de la Receta");

            VerRecetaDespachoController detalleController = loader.getController();
            detalleController.setReceta(seleccionada);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alerta.error("Error","Error al abrir detalle de receta: " + e.getMessage());
        }
    }

    private void cargarRecetas(List<Receta> recetas) {
        recetasObservable = FXCollections.observableArrayList();
        todasLasRecetas = FXCollections.observableArrayList();
        if (recetas != null) {
            recetasObservable.addAll(recetas);
            todasLasRecetas.addAll(recetas);
        }
        tblRecetas.setItems(recetasObservable);
    }

    @FXML
    private void Volver(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/dashboard.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void Buscar() {
        String textoBusqueda = txtBuscar.getText();
        if (textoBusqueda == null || textoBusqueda.trim().isEmpty()) {
            recetasObservable.clear();
            recetasObservable.addAll(todasLasRecetas);
            return;
        }

        try {
            String filtro = cmbFiltrar.getValue();
            if (filtro == null) filtro = "Todos"; // 🔹 fallback
            String criterio = textoBusqueda.trim().toLowerCase();

            List<Receta> resultados = new ArrayList<>();

            switch (filtro) {
                case "ID Receta":
                    resultados = todasLasRecetas.stream()
                            .filter(r -> r.getId() != null &&
                                    r.getId().toLowerCase().contains(criterio))
                            .toList();
                    break;

                case "Paciente":
                    resultados = controller.buscarPorPaciente(Sesion.getUsuario(), textoBusqueda.trim());
                    break;

                case "Médico":
                    resultados = controller.buscarPorMedico(Sesion.getUsuario(), textoBusqueda.trim());
                    break;

                case "Medicamento":
                    resultados = todasLasRecetas.stream()
                            .filter(r -> r.getPrimerMedicamento() != null &&
                                    r.getPrimerMedicamento().toLowerCase().contains(criterio))
                            .toList();
                    break;

                case "Estado":
                    try {
                        EstadoReceta estado = EstadoReceta.valueOf(textoBusqueda.trim().toUpperCase());
                        resultados = controller.buscarPorEstado(Sesion.getUsuario(), estado);
                    } catch (IllegalArgumentException ex) {
                        resultados = todasLasRecetas.stream()
                                .filter(r -> r.getEstado() != null &&
                                        r.getEstado().toString().toLowerCase().contains(criterio))
                                .toList();
                    }
                    break;

                case "Todos":
                default:
                    resultados = todasLasRecetas.stream()
                            .filter(r ->
                                    (r.getId() != null && r.getId().toLowerCase().contains(criterio)) ||
                                            (r.getPrimerMedicamento() != null && r.getPrimerMedicamento().toLowerCase().contains(criterio)) ||
                                            (r.getPresentacionPrimerMedicamento() != null && r.getPresentacionPrimerMedicamento().toLowerCase().contains(criterio)) ||
                                            (r.getEstado() != null && r.getEstado().toString().toLowerCase().contains(criterio)) ||
                                            (r.getPaciente() != null && r.getPaciente().getNombre() != null &&
                                                    r.getPaciente().getNombre().toLowerCase().contains(criterio)) ||
                                            (r.getMedico() != null && r.getMedico().getNombre() != null &&
                                                    r.getMedico().getNombre().toLowerCase().contains(criterio))
                            )
                            .toList();
                    break;
            }

            recetasObservable.clear();
            recetasObservable.addAll(resultados);

        } catch (Exception e) {
            Alerta.error("Error", "Error al buscar recetas: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
