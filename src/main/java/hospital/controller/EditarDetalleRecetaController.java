package hospital.controller;

import hospital.controller.busqueda.Async;
import hospital.logica.RecetaLogica;
import hospital.logica.Sesion;
import hospital.model.*;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;

public class EditarDetalleRecetaController {

    @FXML
    private ComboBox<EstadoReceta> btnFiltro;

    @FXML
    private Button btnCambiarEstado;

    @FXML
    private Button btnVolver;

    @FXML
    private ProgressIndicator progressIndicator;

    private Receta receta;
    private final RecetaLogica recetaIntermediaria = new RecetaLogica();

    public void initialize() {
        btnFiltro.setItems(FXCollections.observableArrayList(EstadoReceta.values()));
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }
    }

    public void setReceta(Receta receta) {
        this.receta = receta;
        if (receta != null) {
            btnFiltro.setValue(receta.getEstado());
        }
    }

    @FXML
    private void cambiarEstado(ActionEvent event) {
        if (receta == null || btnFiltro.getValue() == null) {
            Alerta.error("Error", "Debe seleccionar un estado.");
            return;
        }

        Usuario usuarioActual = Sesion.getUsuario();
        if (!(usuarioActual instanceof Farmaceuta)) {
            Alerta.error("Error", "Solo los farmaceutas pueden cambiar el estado de una receta.");
            return;
        }

        cambiarEstadoAsync((Farmaceuta) usuarioActual, btnFiltro.getValue());
    }

    private void cambiarEstadoAsync(Farmaceuta farmaceuta, EstadoReceta nuevoEstado) {
        deshabilitarControles(true);
        mostrarCargando(true);

        // Capturar ID de receta antes del hilo
        final String recetaId = receta.getId();

        Async.Run(
                // Tarea en segundo plano
                () -> {
                    try {
                        recetaIntermediaria.actualizarEstado(farmaceuta, recetaId, nuevoEstado);
                        return nuevoEstado;
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                },

                // OnSuccess
                estado -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);

                    // Actualizar objeto local
                    receta.setEstado(estado);

                    Alerta.info("Éxito", "Estado actualizado a: " + estado);
                    cerrarVentana();
                },

                // OnError
                error -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.error("Error", "No se pudo actualizar: " + error.getMessage());
                }
        );
    }

    @FXML
    private void Volver(ActionEvent event) {
        cerrarVentana();
    }

    private void cerrarVentana() {
        ((Stage) btnVolver.getScene().getWindow()).close();
    }

    private void deshabilitarControles(boolean deshabilitar) {
        btnFiltro.setDisable(deshabilitar);
        btnCambiarEstado.setDisable(deshabilitar);
        btnVolver.setDisable(deshabilitar);
    }

    private void mostrarCargando(boolean mostrar) {
        if (progressIndicator != null) {
            progressIndicator.setVisible(mostrar);
        }
    }
}