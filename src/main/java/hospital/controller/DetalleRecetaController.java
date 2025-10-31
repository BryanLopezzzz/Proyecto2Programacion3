package hospital.controller;

import hospital.controller.busqueda.Async;
import hospital.logica.MedicamentoLogica;
import hospital.model.Administrador;
import hospital.model.DetalleReceta;
import hospital.model.Medicamento;
import hospital.model.Medico;
import hospital.model.Receta;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class DetalleRecetaController {

    @FXML private TextArea txtIndicaciones;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtDuracion;
    @FXML private Button btnAgregar;
    @FXML private Button btnVolver;

    private Medico medico;
    private String recetaId;
    private String medicamentoId;

    private final MedicamentoLogica medicamentoIntermediaria = new MedicamentoLogica();

    private DetalleReceta detalleCreado;

    private boolean modoEdicion = false;
    private DetalleReceta detalleEditable;

    public void setContext(Medico medico, String recetaId, String medicamentoId) {
        this.medico = medico;
        this.recetaId = recetaId;
        this.medicamentoId = medicamentoId;
    }

    public void setDetalleParaEditar(DetalleReceta detalle) {
        if (detalle == null) return;
        this.modoEdicion = true;
        this.detalleEditable = detalle;
        txtCantidad.setText(String.valueOf(detalle.getCantidad()));
        txtDuracion.setText(String.valueOf(detalle.getDiasTratamiento()));
        txtIndicaciones.setText(detalle.getIndicaciones());
    }

    public void setReceta(Receta receta) {
        // modo solo lectura: deshabilitar todo si lo requiere
        txtCantidad.setEditable(false);
        txtDuracion.setEditable(false);
        txtIndicaciones.setEditable(false);
        btnAgregar.setVisible(false);

        if (receta != null && !receta.getDetalles().isEmpty()) {
            var primer = receta.getDetalles().get(0);
            txtCantidad.setText(String.valueOf(primer.getCantidad()));
            txtDuracion.setText(String.valueOf(primer.getDiasTratamiento()));
            txtIndicaciones.setText(primer.getIndicaciones());
        }
    }

    @FXML
    private void Agregar() {
        try {
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            int dias = Integer.parseInt(txtDuracion.getText().trim());
            String indicaciones = txtIndicaciones.getText().trim();

            if (modoEdicion && detalleEditable != null) {
                detalleEditable.setCantidad(cantidad);
                detalleEditable.setDiasTratamiento(dias);
                detalleEditable.setIndicaciones(indicaciones);
                this.detalleCreado = detalleEditable;

                Alerta.info("Informacion", "Actualizado correctamente.");
                cerrarVentana();
            } else {
                agregarDetalleAsync(cantidad, dias, indicaciones);
            }

        } catch (NumberFormatException e) {
            Alerta.error("Entrada inválida", "Cantidad y duración deben ser números enteros.");
        }
    }

    private void agregarDetalleAsync(int cantidad, int dias, String indicaciones) {
        deshabilitarControles(true);

        Async.Run(
                () -> {
                    try {
                        Medicamento medicamentoCompleto = medicamentoIntermediaria.buscarPorCodigo(
                                new Administrador(),
                                medicamentoId
                        );

                        if (medicamentoCompleto == null) {
                            throw new Exception("No se pudo encontrar el medicamento con código: " + medicamentoId);
                        }

                        DetalleReceta nuevo = new DetalleReceta();
                        nuevo.setCantidad(cantidad);
                        nuevo.setDiasTratamiento(dias);
                        nuevo.setIndicaciones(indicaciones);
                        nuevo.setMedicamento(medicamentoCompleto);

                        return nuevo;
                    } catch (Exception e) {
                        throw new RuntimeException("Error al procesar el medicamento: " + e.getMessage(), e);
                    }
                },
                // OnSuccess
                nuevoDetalle -> {
                    this.detalleCreado = nuevoDetalle;
                    deshabilitarControles(false);
                    Alerta.info("Informacion", "Agregado correctamente.");
                    cerrarVentana();
                },
                // OnError
                error -> {
                    deshabilitarControles(false);
                    Alerta.error("Error", error.getMessage());
                }
        );
    }

    private void deshabilitarControles(boolean deshabilitar) {
        txtCantidad.setDisable(deshabilitar);
        txtDuracion.setDisable(deshabilitar);
        txtIndicaciones.setDisable(deshabilitar);
        btnAgregar.setDisable(deshabilitar);
        btnVolver.setDisable(deshabilitar);
    }

    @FXML
    private void Volver() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.close();
    }

    public DetalleReceta getDetalleCreado() {
        return detalleCreado;
    }
}