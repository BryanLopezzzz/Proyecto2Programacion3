package hospital.controller.registro;


import hospital.controller.Alerta;
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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegistroMedicoController {

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

    @FXML
    public void initialize() {
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }
    }

    @FXML
    public void Guardar(ActionEvent event) {
            if (!validarCampos()) {
                return;
            }

        String id = txtIdentificacion.getText().trim();
        String nombre = txtNombre.getText().trim();
        String especialidad = txtEspecialidad.getText().trim();

        guardarAsync(id, nombre, especialidad);
    }

    private void guardarAsync(String id, String nombre, String especialidad) {
        deshabilitarControles(true);
        mostrarCargando(true);
        Async.Run(
                () -> {
                    try {
                        Medico nuevoMedico = new Medico();
                        nuevoMedico.setId(id);
                        nuevoMedico.setNombre(nombre);
                        nuevoMedico.setEspecialidad(especialidad);

                        medicoIntermediaria.agregar(admin, nuevoMedico);
                        return nuevoMedico;
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                },

                // OnSuccess
                medico -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);

                    mostrarInfo("Médico registrado exitosamente.\n" +
                            "ID: " + medico.getId() + "\n" +
                            "Nombre: " + medico.getNombre() + "\n" +
                            "Especialidad: " + medico.getEspecialidad());

                    limpiarCampos();
                    volverABusqueda();
                },

                // OnError
                error -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.error("Error","Error al registrar médico: " + error.getMessage());
                }
        );
    }

    @FXML
    public void Volver(ActionEvent event) {
        volverABusqueda();
    }

    private boolean validarCampos() {
        StringBuilder errores = new StringBuilder();

        String id = txtIdentificacion.getText();
        String nombre = txtNombre.getText();
        String especialidad = txtEspecialidad.getText();

        if (id == null || id.trim().isEmpty()) {
            errores.append("- El ID es obligatorio.\n");
        } else if (id.trim().length() < 2) {
            errores.append("- El ID debe tener al menos 2 caracteres.\n");
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            errores.append("- El nombre es obligatorio.\n");
        } else if (nombre.trim().length() < 2) {
            errores.append("- El nombre debe tener al menos 2 caracteres.\n");
        }

        if (especialidad == null || especialidad.trim().isEmpty()) {
            errores.append("- La especialidad es obligatoria.\n");
        } else if (especialidad.trim().length() < 3) {
            errores.append("- La especialidad debe tener al menos 3 caracteres.\n");
        }

        if (errores.length() > 0) {
            Alerta.error("Error","Por favor corrija los siguientes errores:\n\n" + errores.toString());
            return false;
        }

        return true;
    }

    private void limpiarCampos() {
        txtIdentificacion.clear();
        txtNombre.clear();
        txtEspecialidad.clear();
        txtIdentificacion.requestFocus();
    }

    private void volverABusqueda() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/MedicosAdmin.fxml"));
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
        txtIdentificacion.setDisable(deshabilitar);
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

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registro Exitoso");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

}