package hospital.controller;

import hospital.controller.busqueda.Async;
import hospital.logica.LoginLogica;
import hospital.logica.Sesion;
import hospital.model.Administrador;
import hospital.model.Farmaceuta;
import hospital.model.Medico;
import hospital.model.Usuario;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import hospital.servicios.HospitalClient;

import java.util.function.Consumer;

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

    private static HospitalClient clientGlobal;
    private HospitalClient client;
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
        verificarYReconectar();
        loginAsync(id, clave);
    }

    private void conectarAlServidor() {
        try {
            if (clientGlobal == null) {
                clientGlobal = HospitalClient.getInstance();
            }

            String host = "localhost";
            int port = 5000;

            if (!clientGlobal.isConectado()) {
                clientGlobal.conectar(host, port);
                System.out.println("Conectado al servidor: " + host + ":" + port);
            }

        } catch (Exception e) {
            System.err.println("Error conectando al servidor: " + e.getMessage());
            clientGlobal = null;
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Advertencia");
                alert.setHeaderText("No se pudo conectar al servidor");
                alert.setContentText("El sistema funcionará en modo local.\n" +
                        "Funcionalidades de chat y notificaciones no estarán disponibles.\n\n" +
                        "Error: " + e.getMessage());
                alert.show();
            });
        }
    }

    private void loginAsync(String id, String clave) {
        deshabilitarControles(true);
        mostrarCargando(true);

        if (clientGlobal != null && clientGlobal.isConectado()) {
            loginRemoto(id, clave);
        } else {
            loginLocal(id, clave);
        }
    }

    private void loginRemoto(String id, String clave) {
        Consumer<String> listenerOriginal = null;

        clientGlobal.login(id, clave, respuesta -> {
            Platform.runLater(() -> {
                try {
                    String[] partes = respuesta.split("\\|");

                    if ("OK".equals(partes[0]) && partes.length >= 3) {
                        String nombre = partes[1];
                        String rol = partes[2];

                        Usuario usuario = crearUsuarioPorRol(id, nombre, rol);
                        Sesion.setUsuario(usuario);
                        cargarDashboard();

                    } else {
                        mostrarCargando(false);
                        deshabilitarControles(false);

                        String mensaje = partes.length > 1 ? partes[1] : "Error desconocido";
                        if (mensaje.contains("Credenciales") || mensaje.contains("incorrectas")) {
                            Alerta.error("Error","Usuario o contraseña incorrectos.");
                        } else {
                            Alerta.error("Error","Error de autenticación: " + mensaje);
                        }

                        limpiarCampos();
                    }

                } catch (Exception e) {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.error("Error","Error procesando respuesta: " + e.getMessage());
                    limpiarCampos();
                }
            });
        });
    }

    private void loginLocal(String id, String clave) {
        Async.Run(
                () -> {
                    try {
                        return loginLogica.login(id, clave);
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                },
                // OnSuccess
                usuario -> {
                    Sesion.setUsuario(usuario);
                    cargarDashboard();
                },
                // OnError
                error -> {
                    Platform.runLater(() -> {
                        mostrarCargando(false);
                        deshabilitarControles(false);

                        String mensaje = error.getMessage();
                        if (mensaje.contains("Credenciales incorrectas")) {
                            Alerta.error("Error","Usuario o contraseña incorrectos.");
                        } else {
                            Alerta.error("Error","Error al iniciar sesión: " + mensaje);
                        }

                        limpiarCampos();
                    });
                }
        );
    }

    private Usuario crearUsuarioPorRol(String id, String nombre, String rol) {
        switch (rol) {
            case "ADMINISTRADOR":
                Administrador admin = new Administrador();
                admin.setId(id);
                admin.setNombre(nombre);
                return admin;

            case "MEDICO":
                Medico medico = new Medico();
                medico.setId(id);
                medico.setNombre(nombre);
                return medico;

            case "FARMACEUTA":
                Farmaceuta farmaceuta = new Farmaceuta();
                farmaceuta.setId(id);
                farmaceuta.setNombre(nombre);
                return farmaceuta;

            default:
                throw new RuntimeException("Rol desconocido: " + rol);
        }
    }

    private void cargarDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/dashboard.fxml"));
            Scene scene = new Scene(loader.load());

            DashboardController dashboardController = loader.getController();
            dashboardController.setLoginController(loginLogica);

            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Sistema Hospital - Dashboard");
            dashboardStage.setScene(scene);

            dashboardStage.setOnCloseRequest(event -> {
                if (clientGlobal != null && clientGlobal.isConectado()) {
                    clientGlobal.logout(resp ->
                            System.out.println("Logout: " + resp)
                    );
                    clientGlobal.desconectar();
                    clientGlobal = null;
                }
                Platform.exit();
                System.exit(0);
            });

            dashboardStage.show();

            Platform.runLater(() -> {
                Stage loginStage = (Stage) btnEntrar.getScene().getWindow();
                loginStage.close();
            });

        } catch (Exception e) {
            mostrarCargando(false);
            deshabilitarControles(false);
            Alerta.error("Error","Error al cargar el dashboard: " + e.getMessage());
        }
    }

    private void verificarYReconectar() {
        if (clientGlobal == null || !clientGlobal.isConectado()) {
            System.out.println("Reconectando al servidor...");
            conectarAlServidor();
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

    private void limpiarCampos() {
        Platform.runLater(() -> {
            txtClave.clear();
            txtClaveVisible.clear();
            if (txtClave.isVisible()) {
                txtClave.requestFocus();
            } else {
                txtClaveVisible.requestFocus();
            }
        });
    }
}
