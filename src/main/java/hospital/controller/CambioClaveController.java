package hospital.controller;

import hospital.logica.LoginLogica;
import hospital.model.Usuario;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class CambioClaveController {

    @FXML
    private PasswordField txtContrasenaAnterior;

    @FXML
    private TextField txtContrasenaAnteriorVisible;

    @FXML
    private PasswordField txtNuevaContrasena;

    @FXML
    private TextField txtNuevaContrasenaVisible;

    @FXML
    private PasswordField txtConfirmarContrasena;

    @FXML
    private TextField txtConfirmarContrasenaVisible;

    @FXML
    private Button btnCambiar;

    @FXML
    private Button btnVerAnterior;

    @FXML
    private ImageView iconVerAnterior;

    @FXML
    private Button btnVerNueva;

    @FXML
    private ImageView iconVerNueva;

    @FXML
    private Button btnVerConfirmar;

    @FXML
    private ImageView iconVerConfirmar;

    @FXML
    private Button btnVolver;

    private Usuario usuario;
    private LoginLogica loginLogica;

    private final Image eyeIcon = new Image(getClass().getResourceAsStream("/icons/eye.png"));
    private final Image eyeOffIcon = new Image(getClass().getResourceAsStream("/icons/eye-off.png"));

    public void initialize() {
        setupPasswordToggle(txtContrasenaAnterior, txtContrasenaAnteriorVisible, btnVerAnterior, iconVerAnterior);
        setupPasswordToggle(txtNuevaContrasena, txtNuevaContrasenaVisible, btnVerNueva, iconVerNueva);
        setupPasswordToggle(txtConfirmarContrasena, txtConfirmarContrasenaVisible, btnVerConfirmar, iconVerConfirmar);
    }

    private void setupPasswordToggle(PasswordField passwordField, TextField textField, Button button, ImageView icon) {
        textField.setVisible(false);
        textField.setManaged(false);

        passwordField.textProperty().bindBidirectional(textField.textProperty());

        button.setOnAction(event -> {
            boolean isVisible = textField.isVisible();
            textField.setVisible(!isVisible);
            textField.setManaged(!isVisible);
            passwordField.setVisible(isVisible);
            passwordField.setManaged(isVisible);
            icon.setImage(isVisible ? eyeIcon : eyeOffIcon);
        });
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setLoginController(LoginLogica loginLogica) {
        this.loginLogica = loginLogica;
    }

    @FXML
    private void cambiarContrasena() {
        String actual = txtContrasenaAnterior.getText();
        String nueva = txtNuevaContrasena.getText();
        String confirmar = txtConfirmarContrasena.getText();

        if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
            Alerta.error("Error", "Todos los campos son obligatorios.");
            return;
        }
        if (!nueva.equals(confirmar)) {
            Alerta.error("Error", "La confirmación no coincide con la nueva contraseña.");
            return;
        }

        try {
            loginLogica.cambiarClave(actual, nueva);
            Alerta.info("Éxito", "Contraseña cambiada correctamente.");
            limpiarCampos();
        } catch (Exception e) {
            Alerta.error("Error", e.getMessage());
        }
    }

    private void limpiarCampos() {
        txtContrasenaAnterior.clear();
        txtNuevaContrasena.clear();
        txtConfirmarContrasena.clear();
    }

    @FXML
    private void Volver() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/dashboard.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Aquí es importante obtener la instancia correcta del LoginController
            // Si DashboardView lo necesita, hay que pasarlo.
            DashboardController dashboardController = fxmlLoader.getController();
            dashboardController.setLoginController(this.loginLogica); // Re-establecer el controlador de login

            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alerta.error("Error", "Error al volver al dashboard: " + e.getMessage());
        }
    }
}
