package hospital.controller;

import hospital.controller.busqueda.Async;
import hospital.logica.MedicoLogica;
import hospital.model.Administrador;
import hospital.model.Medico;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditarMedicoController {

    @FXML
    private TextField txtIdentificacion;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtEspecialidad;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnVolver;

    @FXML
    private ProgressIndicator progressIndicator;

    private final MedicoLogica medicoIntermediaria = new MedicoLogica();
    private final Administrador admin = new Administrador();
    private Medico medicoActual;

    @FXML
    public void initialize() {
        // El ID no debe ser editable en la modificación
        txtIdentificacion.setEditable(false);
        txtIdentificacion.setStyle("-fx-background-color: #f0f0f0;");

        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }
        txtNombre.requestFocus();
    }

    public void inicializarConMedico(Medico medico) {
        if (medico == null) {
            mostrarError("No se pudo cargar la información del médico.");
            return;
        }

        this.medicoActual = medico;

        // Cargar los datos en los campos
        txtIdentificacion.setText(medico.getId());
        txtNombre.setText(medico.getNombre());
        txtEspecialidad.setText(medico.getEspecialidad());
    }

    @FXML
    public void Guardar(ActionEvent event) {
        if (!validarCampos()) {
            return;
        }

        // Verificar si hubo cambios
        if (!hayCambios()) {
            mostrarInfo("No se detectaron cambios para guardar.");
            return;
        }

        // Mostrar confirmación antes de guardar
        mostrarConfirmacion("¿Está seguro que desea guardar los cambios?", this::guardarMedicoAsync);
    }

    private void guardarMedicoAsync() {
        deshabilitarControles(true);
        mostrarCargando(true);

        Async.runVoid(
                () -> {
                    try {
                        // Crear médico con los datos actualizados
                        Medico medicoModificado = new Medico();
                        medicoModificado.setId(txtIdentificacion.getText().trim());
                        medicoModificado.setNombre(txtNombre.getText().trim());
                        medicoModificado.setEspecialidad(txtEspecialidad.getText().trim());

                        // Si el médico original tenía clave, la mantenemos
                        if (medicoActual.getClave() != null) {
                            medicoModificado.setClave(medicoActual.getClave());
                        }

                        medicoIntermediaria.modificar(admin, medicoModificado);
                    } catch (Exception e) {
                        throw new RuntimeException("Error al actualizar: " + e.getMessage(), e);
                    }
                },
                // OnSuccess
                () -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    mostrarInfo("Médico modificado exitosamente.");
                    volverABusqueda();
                },
                // OnError
                error -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    mostrarError("Error al guardar médico: " + error.getMessage());
                }
        );
    }

    @FXML
    public void Volver(ActionEvent event) {
        // Verificar si hay cambios sin guardar
        if (hayCambios()) {
            mostrarConfirmacion("Hay cambios sin guardar. ¿Está seguro que desea salir?",
                    this::volverABusqueda);
        } else {
            volverABusqueda();
        }
    }

    private boolean validarCampos() {
        StringBuilder errores = new StringBuilder();

        if (txtIdentificacion.getText() == null || txtIdentificacion.getText().trim().isEmpty()) {
            errores.append("- El ID es obligatorio.\n");
        }

        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            errores.append("- El nombre es obligatorio.\n");
        } else if (txtNombre.getText().trim().length() < 2) {
            errores.append("- El nombre debe tener al menos 2 caracteres.\n");
        }

        if (txtEspecialidad.getText() == null || txtEspecialidad.getText().trim().isEmpty()) {
            errores.append("- La especialidad es obligatoria.\n");
        } else if (txtEspecialidad.getText().trim().length() < 2) {
            errores.append("- La especialidad debe tener al menos 2 caracteres.\n");
        }

        if (errores.length() > 0) {
            mostrarError("Por favor corrija los siguientes errores:\n\n" + errores.toString());
            return false;
        }

        return true;
    }

    private boolean hayCambios() {
        if (medicoActual == null) {
            return false;
        }

        String nombreActual = txtNombre.getText().trim();
        String especialidadActual = txtEspecialidad.getText().trim();

        return !nombreActual.equals(medicoActual.getNombre()) ||
                !especialidadActual.equals(medicoActual.getEspecialidad());
    }

    private void volverABusqueda() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/medicosAdmin.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Buscar Médicos");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al volver a la búsqueda de médicos.");
        }
    }

    private void deshabilitarControles(boolean deshabilitar) {
        txtNombre.setDisable(deshabilitar);
        txtEspecialidad.setDisable(deshabilitar);
        btnGuardar.setDisable(deshabilitar);
        btnVolver.setDisable(deshabilitar);
    }

    private void mostrarCargando(boolean mostrar) {
        if (progressIndicator != null) {
            progressIndicator.setVisible(mostrar);
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarConfirmacion(String mensaje, Runnable accion) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        if (alert.showAndWait().get() == ButtonType.OK) {
            accion.run();
        }
    }

}