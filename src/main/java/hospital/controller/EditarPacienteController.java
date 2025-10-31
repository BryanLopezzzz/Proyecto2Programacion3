package hospital.controller;

import hospital.controller.busqueda.Async;
import hospital.logica.PacienteLogica;
import hospital.model.Administrador;
import hospital.model.Paciente;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EditarPacienteController {

    @FXML
    private TextField txtIdentificacion;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtTelefono;

    @FXML
    private DatePicker dtpFechaNac;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnVolver;

    @FXML
    private ProgressIndicator progressIndicator;

    private final PacienteLogica pacienteIntermediaria = new PacienteLogica();
    private final Administrador admin = new Administrador();
    private Paciente pacienteOriginal;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        txtIdentificacion.setEditable(false);
        txtIdentificacion.setStyle(txtIdentificacion.getStyle() + "; -fx-background-color: #f5f5f5;");

        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }

        txtNombre.requestFocus();
    }

    public void cargarPaciente(Paciente paciente) {
        if (paciente != null) {
            this.pacienteOriginal = paciente;
            txtIdentificacion.setText(paciente.getId());
            txtNombre.setText(paciente.getNombre());
            txtTelefono.setText(paciente.getTelefono());
            dtpFechaNac.setValue(paciente.getFechaNacimiento());
        }
    }

    @FXML
    public void Guardar(ActionEvent event) {
        if (!validarCampos()) {
            return;
        }

        Paciente pacienteActualizado = new Paciente();
        pacienteActualizado.setId(txtIdentificacion.getText().trim());
        pacienteActualizado.setNombre(txtNombre.getText().trim());
        pacienteActualizado.setTelefono(txtTelefono.getText().trim());
        pacienteActualizado.setFechaNacimiento(dtpFechaNac.getValue());

        if (sonIguales(pacienteOriginal, pacienteActualizado)) {
            Alerta.info("Información","No se han detectado cambios en los datos del paciente.");
            return;
        }

        Alerta.confirmacion("¿Está seguro que desea guardar los cambios?",
                () -> guardarPacienteAsync(pacienteActualizado));
    }

    private void guardarPacienteAsync(Paciente pacienteActualizado) {
        deshabilitarControles(true);
        mostrarCargando(true);

        Async.runVoid(
                () -> {
                    try {
                        pacienteIntermediaria.modificar(admin, pacienteActualizado);
                    } catch (Exception e) {
                        throw new RuntimeException("Error al actualizar: " + e.getMessage(), e);
                    }
                },
                // OnSuccess
                () -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.info("Información","Paciente actualizado correctamente.");
                    volverABusqueda();
                },
                // OnError
                error -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.error("Error","Error al actualizar paciente: " + error.getMessage());
                }
        );
    }

    @FXML
    public void Volver(ActionEvent event) {
        Paciente pacienteActual = new Paciente();
        pacienteActual.setId(txtIdentificacion.getText().trim());
        pacienteActual.setNombre(txtNombre.getText().trim());
        pacienteActual.setTelefono(txtTelefono.getText().trim());
        pacienteActual.setFechaNacimiento(dtpFechaNac.getValue());

        if (!sonIguales(pacienteOriginal, pacienteActual)) {
            Alerta.confirmacion("Hay cambios sin guardar. ¿Está seguro que desea salir?",
                    this::volverABusqueda);
        } else {
            volverABusqueda();
        }
    }

    private boolean validarCampos() {
        StringBuilder errores = new StringBuilder();

        if (txtIdentificacion.getText().trim().isEmpty()) {
            errores.append("- El ID no puede estar vacío.\n");
        }

        if (txtNombre.getText().trim().isEmpty()) {
            errores.append("- El nombre es obligatorio.\n");
        } else if (txtNombre.getText().trim().length() < 2) {
            errores.append("- El nombre debe tener al menos 2 caracteres.\n");
        }

        if (txtTelefono.getText().trim().isEmpty()) {
            errores.append("- El teléfono es obligatorio.\n");
        } else if (txtTelefono.getText().trim().length() < 8) {
            errores.append("- El teléfono debe tener al menos 8 dígitos.\n");
        }

        if (dtpFechaNac.getValue() == null) {
            errores.append("- La fecha de nacimiento es obligatoria.\n");
        } else if (dtpFechaNac.getValue().isAfter(LocalDate.now())) {
            errores.append("- La fecha de nacimiento no puede ser futura.\n");
        }

        if (errores.length() > 0) {
            Alerta.error("Error","Por favor corrija los siguientes errores:\n\n" + errores.toString());
            return false;
        }

        return true;
    }

    private boolean sonIguales(Paciente original, Paciente actualizado) {
        if (original == null || actualizado == null) return false;

        return original.getId().equals(actualizado.getId()) &&
                original.getNombre().equals(actualizado.getNombre()) &&
                original.getTelefono().equals(actualizado.getTelefono()) &&
                original.getFechaNacimiento().equals(actualizado.getFechaNacimiento());
    }

    private void volverABusqueda() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/pacientesAdmin.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Buscar Pacientes");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alerta.error("Error","Error al volver a la vista de búsqueda.");
        }
    }

    private void deshabilitarControles(boolean deshabilitar) {
        txtNombre.setDisable(deshabilitar);
        txtTelefono.setDisable(deshabilitar);
        dtpFechaNac.setDisable(deshabilitar);
        btnGuardar.setDisable(deshabilitar);
        btnVolver.setDisable(deshabilitar);
    }

    private void mostrarCargando(boolean mostrar) {
        if (progressIndicator != null) {
            progressIndicator.setVisible(mostrar);
        }
    }

    public Paciente getPacienteOriginal() {
        return pacienteOriginal;
    }
}