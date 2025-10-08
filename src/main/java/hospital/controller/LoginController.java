package hospital.controller;

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

        try {
            Usuario usuario = loginLogica.login(id, clave);
            Sesion.setUsuario(usuario);

            // cargar la vista principal(esto puede cambiar si el menu correcto no es ese)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/dashboard.fxml"));
            Scene scene = new Scene(loader.load());

            // mandar el usuario al dashboard
            DashboardController dashboardController = loader.getController();
            dashboardController.setLoginController(loginLogica);

            Stage stage = new Stage();
            stage.setTitle("Menú Principal");
            stage.setScene(scene);
            stage.show();

            // cerrar la ventana de login
            Stage loginStage = (Stage) btnEntrar.getScene().getWindow();
            loginStage.close();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
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
}
