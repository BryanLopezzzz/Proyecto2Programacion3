package hospital.controller;

import hospital.controller.busqueda.Async;
import hospital.logica.LoginLogica;
import hospital.logica.Sesion;
import hospital.model.Usuario;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField txtUsuario;
    @FXML
    private PasswordField txtClave;
    @FXML
    private TextField txtClaveVisible;
    @FXML
    private Button btnVerClave;
    @FXML
    private javafx.scene.image.ImageView imgVerClave;
    @FXML
    private Button btnEntrar;
    @FXML //Es lo nuevo de hilos
    private ProgressIndicator progressIndicator;

    private final LoginLogica loginLogica = new LoginLogica();
    private boolean claveVisible = false;

    @FXML
    private void login() {
        String id = txtUsuario.getText();
        String clave = claveVisible ? txtClaveVisible.getText() : txtClave.getText();

        if (id == null || id.trim().isEmpty()) {
            Alerta.error("Error","El ID de usuario es obligatorio.");
            return;
        }

        if (clave == null || clave.trim().isEmpty()) {
            Alerta.error("Error","El clave es obligatorio.");
            return;
        }

        loginAsync(id, clave);
    }

    private void loginAsync(String id, String clave) {
        deshabilitarControles(true);
        mostrarCargando(true);

        Async.Run(
                 () -> {
                    try {
                        return loginLogica.login(id, clave);
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                },

                // OnSuccess - Se ejecuta en el hilo de JavaFX
                usuario -> {
                    Sesion.setUsuario(usuario);

                    try {
                        // Cargar dashboard
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/dashboard.fxml"));
                        Scene scene = new Scene(loader.load());

                        // Pasar el LoginLogica al dashboard
                        DashboardController dashboardController = loader.getController();
                        dashboardController.setLoginController(loginLogica);

                        Stage stage = new Stage();
                        stage.setTitle("Sistema Hospital - Dashboard");
                        stage.setScene(scene);
                        stage.show();

                        // Cerrar ventana de login
                        Stage loginStage = (Stage) btnEntrar.getScene().getWindow();
                        loginStage.close();

                    } catch (Exception e) {
                        mostrarCargando(false);
                        deshabilitarControles(false);
                        mostrarError("Error al cargar el dashboard: " + e.getMessage());
                    }
                },

                // OnError
                error -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);

                    String mensaje = error.getMessage();
                    if (mensaje.contains("Credenciales incorrectas")) {
                        mostrarError("Usuario o contraseña incorrectos.");
                    } else {
                        mostrarError("Error al iniciar sesión: " + mensaje);
                    }

                    // Limpiar contraseña por seguridad
                    txtClave.clear();
                    txtClaveVisible.clear();
                    txtClave.requestFocus();
                }
        );
    }

    @FXML
    private void toggleVisibilidadClave() {
        claveVisible = !claveVisible;

        if (claveVisible) {
            txtClaveVisible.setText(txtClave.getText());
            txtClaveVisible.setVisible(true);
            txtClave.setVisible(false);
            imgVerClave.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("/icons/eye-off.png")));
        } else {
            txtClave.setText(txtClaveVisible.getText());
            txtClave.setVisible(true);
            txtClaveVisible.setVisible(false);
            imgVerClave.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("/icons/eye.png")));
        }
    }
    //Metodos nuevo, verificar si sirve como se espera con hilos
    private void deshabilitarControles(boolean deshabilitar) {
        txtUsuario.setDisable(deshabilitar);
        txtClave.setDisable(deshabilitar);
        txtClaveVisible.setDisable(deshabilitar);
        btnEntrar.setDisable(deshabilitar);
        btnVerClave.setDisable(deshabilitar);
    }

    private void mostrarCargando(boolean mostrar) {
        if (progressIndicator != null) {
            progressIndicator.setVisible(mostrar);
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de autenticación");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
