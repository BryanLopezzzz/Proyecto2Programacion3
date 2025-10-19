package hospital.controller;

import hospital.controller.busqueda.Async;
import hospital.controller.busqueda.BuscarPacientePreescripcionController;
import hospital.logica.RecetaLogica;
import hospital.logica.Sesion;
import javafx.collections.FXCollections;
import hospital.model.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.util.List;

public class DespachoController {
    @FXML
    private TextField txtBuscar;

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
    private Button btnBuscar;

    @FXML
    private ProgressIndicator progressIndicator;

    private Paciente pacienteSeleccionado;

    private final RecetaLogica recetaIntermediaria = new RecetaLogica();
    private final ObservableList<Receta> recetasObservable = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configuración de columnas
        colIdentificacionReceta.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getId())
        );

        colNombreMedicamento.setCellValueFactory(data -> {
            if (!data.getValue().getDetalles().isEmpty()) {
                return new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getDetalles().get(0).getMedicamento().getNombre()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });

        colPresentacion.setCellValueFactory(data -> {
            if (!data.getValue().getDetalles().isEmpty()) {
                return new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getDetalles().get(0).getMedicamento().getPresentacion()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });

        colFechaConfeccion.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getFecha() != null ? data.getValue().getFecha().toString() : "-"
                )
        );

        colEstado.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getEstado().name())
        );

        tblRecetas.setItems(recetasObservable);

        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }

        cargarRecetasAsync();
    }

    private void cargarRecetasAsync() {
        deshabilitarControles(true);
        mostrarCargando(true);

        Async.Run(
                () -> {
                    try {
                        return recetaIntermediaria.listar();
                    } catch (Exception e) {
                        throw new RuntimeException("Error al cargar recetas: " + e.getMessage(), e);
                    }
                },
                // OnSuccess
                recetas -> {
                    recetasObservable.setAll(recetas);
                    mostrarCargando(false);
                    deshabilitarControles(false);
                },
                // OnError
                error -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.error("Error", "Error cargando recetas: " + error.getMessage());
                }
        );
    }

    @FXML
    private void BuscarPaciente(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/buscarPacientePrescripcion.fxml"));
            Parent root = loader.load();

            BuscarPacientePreescripcionController buscarView = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Buscar Paciente");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(btnBuscar.getScene().getWindow());

            stage.showAndWait();

            Paciente pacienteNuevo = buscarView.getPacienteSeleccionado();
            if (pacienteNuevo != null) {
                pacienteSeleccionado = pacienteNuevo;
                txtBuscar.setText(pacienteSeleccionado.getNombre() + " (" + pacienteSeleccionado.getId() + ")");
                filtrarRecetasPorPacienteAsync();
                Alerta.info("Paciente seleccionado",
                        "Mostrando recetas para: " + pacienteSeleccionado.getNombre());
            }

        } catch (IOException e) {
            Alerta.error("Error", "No se pudo abrir la ventana de búsqueda de pacientes: " + e.getMessage());
        }
    }

    private void filtrarRecetasPorPacienteAsync() {
        if (pacienteSeleccionado == null) return;

        deshabilitarControles(true);
        mostrarCargando(true);

        final String pacienteId = pacienteSeleccionado.getId();

        Async.Run(
                () -> {
                    try {
                        return recetaIntermediaria.listarRecetasPorPaciente(pacienteId);
                    } catch (Exception e) {
                        throw new RuntimeException("Error al filtrar recetas: " + e.getMessage(), e);
                    }
                },
                // OnSuccess
                recetas -> {
                    recetasObservable.setAll(recetas);
                    mostrarCargando(false);
                    deshabilitarControles(false);
                },
                // OnError
                error -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.error("Error", "Error filtrando recetas del paciente: " + error.getMessage());
                }
        );
    }

    @FXML
    public void VerDetalle(ActionEvent event) {
        Receta seleccionada = tblRecetas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            Alerta.info("Detalle", "Debe seleccionar una receta de la lista.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/verRecetaDespacho.fxml"));
            Parent root = loader.load();
            VerRecetaDespachoController controller = loader.getController();
            controller.setReceta(seleccionada);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Detalle de Receta");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alerta.error("Error", "No se pudo cargar el detalle de la receta.");
        }
    }

    @FXML
    private void CambiarEstado(ActionEvent event) {
        Receta seleccionada = tblRecetas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            Alerta.info("Error", "Seleccione una receta primero.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/editarEstadoReceta.fxml"));
            Parent root = loader.load();
            EditarDetalleRecetaController controller = loader.getController();
            controller.setReceta(seleccionada);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Cambiar Estado");
            stage.showAndWait();

            // Recargar recetas después de cambiar estado
            if (pacienteSeleccionado != null) {
                filtrarRecetasPorPacienteAsync();
            } else {
                cargarRecetasAsync();
            }

        } catch (Exception e) {
            Alerta.error("Error", "No se pudo abrir la ventana.");
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
            Alerta.error("Error", "Error al cargar el dashboard.");
        }
    }

    private void deshabilitarControles(boolean deshabilitar) {
        btnBuscar.setDisable(deshabilitar);
        btnVerDetalle.setDisable(deshabilitar);
        btnVolver.setDisable(deshabilitar);
        txtBuscar.setDisable(deshabilitar);
        tblRecetas.setDisable(deshabilitar);
    }

    private void mostrarCargando(boolean mostrar) {
        if (progressIndicator != null) {
            progressIndicator.setVisible(mostrar);
        }
    }
}