package hospital.controller;

import hospital.controller.busqueda.Async;
import hospital.logica.FarmaceutaLogica;
import hospital.model.Administrador;
import hospital.model.Farmaceuta;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EditarFarmaceutaController {

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private TextField txtIdentificacionFarmaceuta;

    @FXML
    private TextField txtNombreFarmaceuta;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnVolver;

    private final FarmaceutaLogica farmaceutaIntermediaria = new FarmaceutaLogica();
    private final Administrador admin = new Administrador();

    private Farmaceuta farmaceutaOriginal;

    @FXML
    public void initialize() {
        configurarValidaciones();
        configurarCampos();
    }

    private void configurarValidaciones() {
        txtNombreFarmaceuta.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*")) {
                txtNombreFarmaceuta.setText(oldValue);
            }
        });
    }

    private void configurarCampos() {
        txtIdentificacionFarmaceuta.setEditable(false);
        txtIdentificacionFarmaceuta.setStyle(txtIdentificacionFarmaceuta.getStyle() + "; -fx-background-color: #f0f0f0;");

        txtNombreFarmaceuta.requestFocus();
    }

    public void cargarFarmaceuta(Farmaceuta farmaceuta) {
        if (farmaceuta == null) {
            Alerta.error("Error","No se pudo cargar el farmaceuta para edición.");
            return;
        }

        this.farmaceutaOriginal = farmaceuta;

        txtIdentificacionFarmaceuta.setText(farmaceuta.getId());
        txtNombreFarmaceuta.setText(farmaceuta.getNombre());
    }

    @FXML
    public void Guardar(ActionEvent event) {
        if (!validarCampos()) {
            return;
        }

        // Verificar si hubo cambios
        String nuevoNombre = txtNombreFarmaceuta.getText().trim();
        if (farmaceutaOriginal != null && nuevoNombre.equals(farmaceutaOriginal.getNombre())) {
            Alerta.info("Información","No se detectaron cambios para guardar.");
            return;
        }

        Alerta.confirmacion("¿Está seguro que desea guardar los cambios?", () -> {
            guardarFarmaceutaAsync(nuevoNombre);
        });
    }

    private void guardarFarmaceutaAsync(String nuevoNombre) {
        deshabilitarControles(true);
        mostrarCargando(true);

        Async.runVoid(
                () -> {
                    try {
                        Farmaceuta farmaceutaActualizado = new Farmaceuta();
                        farmaceutaActualizado.setId(txtIdentificacionFarmaceuta.getText().trim());
                        farmaceutaActualizado.setNombre(nuevoNombre);

                        // Si el farmaceuta original tiene clave, mantenerla
                        if (farmaceutaOriginal != null && farmaceutaOriginal.getClave() != null) {
                            farmaceutaActualizado.setClave(farmaceutaOriginal.getClave());
                        }

                        // Actualizar usando el controller
                        farmaceutaIntermediaria.modificar(admin, farmaceutaActualizado);
                    } catch (Exception e) {
                        throw new RuntimeException("Error al actualizar: " + e.getMessage(), e);
                    }
                },
                // OnSuccess
                () -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.info("Información","Farmaceuta actualizado exitosamente.");
                    volverABusqueda();
                },
                // OnError
                error -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.error("Error","Error al actualizar farmaceuta: " + error.getMessage());
                }
        );
    }

    private void deshabilitarControles(boolean deshabilitar) {
        txtNombreFarmaceuta.setDisable(deshabilitar);
        btnGuardar.setDisable(deshabilitar);
        btnVolver.setDisable(deshabilitar);
    }

    private void mostrarCargando(boolean mostrar) {
        if (progressIndicator != null) {
            progressIndicator.setVisible(mostrar);
        }
    }

    @FXML
    public void Volver(ActionEvent event) {
        if (hayCambiosSinGuardar()) {
            Alerta.confirmacion("Hay cambios sin guardar. ¿Está seguro que desea salir?",
                    this::volverABusqueda);
        } else {
            volverABusqueda();
        }
    }

    private boolean validarCampos() {
        StringBuilder errores = new StringBuilder();

        String id = txtIdentificacionFarmaceuta.getText().trim();
        if (id.isEmpty()) {
            errores.append("- El ID no puede estar vacío.\n");
        }


        String nombre = txtNombreFarmaceuta.getText().trim();
        if (nombre.isEmpty()) {
            errores.append("- El nombre es obligatorio.\n");
        } else if (nombre.length() < 2) {
            errores.append("- El nombre debe tener al menos 2 caracteres.\n");
        }

        if (errores.length() > 0) {
            Alerta.error("Error","Por favor corrija los siguientes errores:\n\n" + errores.toString());
            return false;
        }

        return true;
    }

    private boolean hayCambiosSinGuardar() {
        if (farmaceutaOriginal == null) {
            return false;
        }

        String nombreActual = txtNombreFarmaceuta.getText().trim();
        return !nombreActual.equals(farmaceutaOriginal.getNombre());
    }

    private void volverABusqueda() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/farmaceutasAdmin.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestión de Farmaceutas");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alerta.error("Error","Error al volver a la vista de búsqueda: " + e.getMessage());
        }
    }


}