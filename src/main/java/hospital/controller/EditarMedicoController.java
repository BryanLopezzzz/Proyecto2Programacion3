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
        txtIdentificacion.setEditable(false);
        txtIdentificacion.setStyle("-fx-background-color: #f0f0f0;");

        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }
        txtNombre.requestFocus();
    }

    public void inicializarConMedico(Medico medico) {
        if (medico == null) {
            Alerta.error("Error","No se pudo cargar la información del médico.");
            return;
        }

        this.medicoActual = medico;

        txtIdentificacion.setText(medico.getId());
        txtNombre.setText(medico.getNombre());
        txtEspecialidad.setText(medico.getEspecialidad());
    }

    @FXML
    public void Guardar(ActionEvent event) {
        if (!validarCampos()) {
            return;
        }


        if (!hayCambios()) {
            Alerta.info("Información","No se detectaron cambios para guardar.");
            return;
        }

        Alerta.confirmacion("¿Está seguro que desea guardar los cambios?", this::guardarMedicoAsync);
    }

    private void guardarMedicoAsync() {
        deshabilitarControles(true);
        mostrarCargando(true);

        Async.runVoid(
                () -> {
                    try {
                        Medico medicoModificado = new Medico();
                        medicoModificado.setId(txtIdentificacion.getText().trim());
                        medicoModificado.setNombre(txtNombre.getText().trim());
                        medicoModificado.setEspecialidad(txtEspecialidad.getText().trim());

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
                    Alerta.info("Información","Médico modificado exitosamente.");
                    volverABusqueda();
                },
                // OnError
                error -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.error("Error","Error al guardar médico: " + error.getMessage());
                }
        );
    }

    @FXML
    public void Volver(ActionEvent event) {
        if (hayCambios()) {
            Alerta.confirmacion("Hay cambios sin guardar. ¿Está seguro que desea salir?",
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
            Alerta.error("Error","Por favor corrija los siguientes errores:\n\n" + errores.toString());
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
            Alerta.error("Error","Error al volver a la búsqueda de médicos.");
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

}