package hospital.controller;

import hospital.controller.busqueda.Async;
import hospital.logica.DashboardLogica;
import hospital.logica.LoginLogica;
import hospital.logica.Sesion;
import hospital.logica.UsuarioManager;
import hospital.model.Medico;
import hospital.model.Usuario;
import hospital.servicios.ServiceProxy;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.Pane;
import java.io.IOException;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import hospital.servicios.HospitalClient;
import javafx.scene.control.ListView;
import javafx.util.Duration;

public class DashboardController {
    @FXML
    private Label lblUsuario;

    @FXML
    private Button btnMedicos;

    @FXML
    private Button btnFarmaceutas;

    @FXML
    private Button btnPacientes;

    @FXML
    private Button btnMedicamentos;

    @FXML
    private Button btnPrescribirReceta;

    @FXML
    private Button btnDespachoReceta;

    @FXML
    private Button btnHistoricoRecetas;

    @FXML
    private Button btnAcercaDe;

    @FXML
    private Button btnCambiarClave;

    @FXML
    private Button btnLogout;

    @FXML
    private Pane paneLineChart;

    @FXML
    private Pane panePieChart;

    @FXML
    private ListView<String> lstUsuariosActivos;

    @FXML
    private Label lblUsuariosConectados;

    @FXML
    private Button btnAbrirChat;

    @FXML
    private ProgressIndicator progressIndicator;

    private HospitalClient client;
    private final DashboardLogica dashboardLogica = new DashboardLogica();
    private final UsuarioManager usuarioManager = new UsuarioManager();
    private LoginLogica loginLogica;

    private final ServiceProxy serviceProxy = ServiceProxy.getInstance();

    public void setLoginController(LoginLogica loginLogica) {
        this.loginLogica = loginLogica;
    }

    public LoginLogica getLoginController() {
        return loginLogica;
    }

    @FXML
    public void initialize() throws SQLException {
        Usuario usuario = Sesion.getUsuario();
        if (usuario != null && lblUsuario != null) {
            lblUsuario.setText(usuario.getNombre());
        }
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }
        configurarPermisosPorRol(usuario);
        cargarGraficosAsync(usuario);

        inicializarChat();
    }


    private void inicializarChat() {
        if (!serviceProxy.isConectado()) {
            return;
        }

        serviceProxy.setOnMensajeRecibido(mensaje -> {
            switch (mensaje.getTipo()) {
                case USUARIO_CONECTADO:
                case USUARIO_DESCONECTADO:
                    Alerta.notificacionTemporal("Sistema", mensaje.getContenido());
                    actualizarUsuariosActivos();
                    break;

                case MENSAJE:
                    mostrarMensajeRecibido(
                            mensaje.getRemitente(),
                            mensaje.getContenido()
                    );
                    break;
            }
        });
        actualizarUsuariosActivos();
    }


    private void procesarNotificacion(String mensaje) {
        Platform.runLater(() -> {
            try {
                String[] partes = mensaje.split("\\|");
                String tipo = partes[0];

                if ("NOTIFICACION".equals(tipo) && partes.length >= 5) {
                    String accion = partes[1];
                    String usuarioNombre = partes[3];

                    if ("LOGIN".equals(accion)) {
                        Alerta.notificacionTemporal("Usuario conectado",
                                usuarioNombre + " se ha conectado al sistema");
                        actualizarUsuariosActivos();
                    } else if ("LOGOUT".equals(accion)) {
                        Alerta.notificacionTemporal("Usuario desconectado",
                                usuarioNombre + " se ha desconectado");
                        actualizarUsuariosActivos();
                    }
                } else if ("MENSAJE".equals(tipo) && partes.length >= 4) {
                    String remitenteNombre = partes[2];
                    String texto = partes[3];

                    mostrarMensajeRecibido(remitenteNombre, texto);
                }

            } catch (Exception e) {
                System.err.println("Error procesando notificación: " + e.getMessage());
            }
        });
    }

    private void actualizarUsuariosActivos() {
        if (!serviceProxy.isConectado()) {
            return;
        }

        serviceProxy.listarUsuariosActivos(
                usuarios -> {
                    javafx.collections.ObservableList<String> nombres =
                            javafx.collections.FXCollections.observableArrayList();

                    for (ServiceProxy.UsuarioActivo usuario : usuarios) {
                        nombres.add(usuario.getNombre() + " (" + usuario.getRol() + ")");
                    }

                    if (lstUsuariosActivos != null) {
                        lstUsuariosActivos.setItems(nombres);
                    }

                    if (lblUsuariosConectados != null) {
                        lblUsuariosConectados.setText(usuarios.size() + " usuarios conectados");
                    }
                },
                error -> {
                    System.err.println("Error actualizando usuarios: " + error);
                }
        );
    }

    private void mostrarMensajeRecibido(String remitente, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Nuevo Mensaje");
        alert.setHeaderText("Mensaje de: " + remitente);
        alert.setContentText(mensaje);

        ButtonType btnResponder = new ButtonType("Responder");
        ButtonType btnCerrar = new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnResponder, btnCerrar);

        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == btnResponder) {
            abrirChat();
        }
    }

    @FXML
    public void abrirChat() {
        try {
            if (!serviceProxy.isConectado()) {
                Alerta.error("Error", "No hay conexión con el servidor.\n" +
                        "La función de mensajería requiere conexión al servidor.");
                return;
            }
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/hospital/view/chat.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Mensajería - Sistema Hospital");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.NONE);
            stage.initOwner(btnAbrirChat.getScene().getWindow());
            stage.setOnCloseRequest(event -> {
                System.out.println("Ventana de chat cerrada");
            });

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error cargando chat.fxml: " + e.getMessage());
            Alerta.error("Error", "No se pudo abrir la ventana de chat.\n" +
                    "Verifique que el archivo chat.fxml existe en /hospital/view/\n" +
                    "Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Alerta.error("Error", "Error inesperado al abrir chat: " + e.getMessage());
        }
    }

    private void cargarGraficosAsync(Usuario usuario) {
        if (usuario == null) return;

        mostrarCargando(true);

        Async.Run(
                () -> {
                    try {
                        Map<String, Integer> datosLineas = dashboardLogica.contarMedicamentosPorMes(
                                usuario,
                                YearMonth.now().minusMonths(5),
                                YearMonth.now()
                        );

                        Map<String, Long> datosPastel = dashboardLogica.contarRecetasPorEstado(usuario);

                        return new DatosGraficos(datosLineas, datosPastel);
                    } catch (Exception e) {
                        throw new RuntimeException("Error al cargar estadísticas: " + e.getMessage(), e);
                    }
                },
                // OnSuccess
                datos -> {
                    mostrarGraficoLineas(usuario, datos.datosLineas);
                    mostrarGraficoPastel(usuario, datos.datosPastel);
                    mostrarCargando(false);
                },
                // OnError
                error -> {
                    mostrarCargando(false);
                    mostrarGraficoLineas(usuario, new LinkedHashMap<>());
                    mostrarGraficoPastel(usuario, new LinkedHashMap<>());
                }
        );
    }
    private void configurarPermisosPorRol(Usuario usuario) throws SQLException {
        if (usuario == null || usuario.getId() == null) {
            ocultarTodosLosBotones();
            return;
        }

        UsuarioManager.TipoUsuario tipo = usuarioManager.determinarTipoUsuario(usuario.getId());

        switch (tipo) {
            case MEDICO:
                configurarPermisosMedico();
                break;
            case FARMACEUTA:
                configurarPermisosFarmaceuta();
                break;
            case ADMINISTRADOR:
                configurarPermisosAdministrador();
                break;
            default:
                ocultarTodosLosBotones();
                System.err.println("Tipo de usuario no reconocido: " + usuario.getId());
                break;
        }
    }

    private void configurarPermisosAdministrador() {
        btnMedicos.setDisable(false);
        btnFarmaceutas.setDisable(false);
        btnPacientes.setDisable(false);
        btnMedicamentos.setDisable(false);

        btnPrescribirReceta.setDisable(true);
        btnDespachoReceta.setDisable(true);
        btnHistoricoRecetas.setDisable(false);
        btnCambiarClave.setDisable(false);
        btnAcercaDe.setDisable(false);
        btnLogout.setDisable(false);
    }

    private void configurarPermisosMedico() {
        btnMedicos.setDisable(true);
        btnFarmaceutas.setDisable(true);
        btnPacientes.setDisable(true);
        btnMedicamentos.setDisable(true);
        btnDespachoReceta.setDisable(true);

        btnPrescribirReceta.setDisable(false);
        btnHistoricoRecetas.setDisable(false);
        btnCambiarClave.setDisable(false);
        btnAcercaDe.setDisable(false);
        btnLogout.setDisable(false);
    }

    private void configurarPermisosFarmaceuta() {
        btnMedicos.setDisable(true);
        btnFarmaceutas.setDisable(true);
        btnPacientes.setDisable(true);
        btnMedicamentos.setDisable(true);
        btnPrescribirReceta.setDisable(true);

        btnDespachoReceta.setDisable(false);
        btnHistoricoRecetas.setDisable(false);
        btnCambiarClave.setDisable(false);
        btnAcercaDe.setDisable(false);
        btnLogout.setDisable(false);
    }

    private void ocultarTodosLosBotones() {
        btnMedicos.setDisable(true);
        btnFarmaceutas.setDisable(true);
        btnPacientes.setDisable(true);
        btnMedicamentos.setDisable(true);
        btnPrescribirReceta.setDisable(true);
        btnDespachoReceta.setDisable(true);

        btnHistoricoRecetas.setDisable(true);
        btnCambiarClave.setDisable(true);
        btnAcercaDe.setDisable(true);
        btnLogout.setDisable(true);
    }

    private void mostrarGraficoLineas(Usuario usuario, Map<String, Integer> datos) {
        if (usuario == null) return;

        Platform.runLater(() -> {
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setTitle("Medicamentos por mes");
            lineChart.setPrefWidth(350);
            lineChart.setPrefHeight(250);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Medicamentos");

            if (datos != null && !datos.isEmpty()) {
                datos.forEach((mes, cantidad) ->
                        series.getData().add(new XYChart.Data<>(mes, cantidad))
                );
            } else {
                series.getData().add(new XYChart.Data<>("Sin datos", 0));
            }

            lineChart.getData().add(series);
            paneLineChart.getChildren().clear();
            paneLineChart.getChildren().add(lineChart);
        });
    }

    private void mostrarGraficoPastel(Usuario usuario, Map<String, Long> datos) {
        if (usuario == null) return;

        Platform.runLater(() -> {
            PieChart pieChart = new PieChart();
            pieChart.setTitle("Recetas por estado");
            pieChart.setPrefWidth(350);
            pieChart.setPrefHeight(250);

            if (datos != null && !datos.isEmpty()) {
                datos.forEach((estado, cantidad) -> {
                    if (cantidad > 0) {
                        pieChart.getData().add(new PieChart.Data(estado, cantidad));
                    }
                });
            }

            if (pieChart.getData().isEmpty()) {
                pieChart.getData().add(new PieChart.Data("Sin datos", 1));
            }

            panePieChart.getChildren().clear();
            panePieChart.getChildren().add(pieChart);
        });
    }

    @FXML
    public void irAMedicos() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/medicosAdmin.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) btnMedicos.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Administración de Médicos");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alerta.error("Error", "Error al cargar la vista de médicos");
        }
    }

    @FXML
    public void irAFarmaceutas() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/farmaceutasAdmin.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) btnFarmaceutas.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Administración de Farmaceutas");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alerta.error("Error", "Error al cargar la vista de farmaceutas");
        }
    }

    @FXML
    public void irAPacientes() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/pacientesAdmin.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) btnPacientes.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Administración de Pacientes");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alerta.error("Error", "Error al cargar la vista de pacientes");
        }
    }

    @FXML
    public void irAMedicamentos() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/medicamentosAdmin.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) btnMedicamentos.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Administración de Medicamentos");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alerta.error("Error", "Error al cargar la vista de medicamentos");
        }
    }

    @FXML
    public void irAPreescribirReceta() {
        try {
            Usuario usuarioActual = Sesion.getUsuario();

            UsuarioManager.TipoUsuario tipo = usuarioManager.determinarTipoUsuario(usuarioActual.getId());
            if (tipo != UsuarioManager.TipoUsuario.MEDICO) {
                Alerta.error("Acceso denegado", "Solo los médicos pueden prescribir recetas.");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/prescribirReceta.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            PreescribirRecetaController controller = fxmlLoader.getController();

            Medico medico = new Medico();
            medico.setId(usuarioActual.getId());
            medico.setNombre(usuarioActual.getNombre());

            controller.setMedico(medico);

            Stage stage = (Stage) btnPrescribirReceta.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Prescribir Receta");
            stage.show();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            Alerta.error("Error", "Error al cargar la vista de prescribir receta: " + e.getMessage());
        }
    }

    @FXML
    public void irADespacho() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/despachoFarmaceuta.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) btnDespachoReceta.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Despacho de recetas");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alerta.error("Error", "Error al cargar la vista de Despacho.");
        }
    }

    @FXML
    public void irAHistoricoRecetas() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/historicoRecetas.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) btnHistoricoRecetas.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Historial de Recetas");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alerta.error("Error", "Error al cargar la vista de historial de recetas.");
        }
    }

    @FXML
    public void irAAcercaDe() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/acercaDe1.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) btnAcercaDe.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Acerca de");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alerta.error("Error", "Error al cargar la vista de Acerca de.");
        }
    }

    @FXML
    public void irACambiarClave() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/cambiarContraseña.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            CambioClaveController cambioClaveController = fxmlLoader.getController();
            cambioClaveController.setUsuario(Sesion.getUsuario());
            cambioClaveController.setLoginController(loginLogica);

            Stage stage = (Stage) btnCambiarClave.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Cambiar Contraseña");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alerta.error("Error", "Error al cargar la vista de cambio de contraseña.");
        }
    }
    @FXML
    public void logout() {
        if (serviceProxy.isConectado()) {
            serviceProxy.logout(exito -> {
                System.out.println("Logout del servidor: " + (exito ? "OK" : "Error"));
            });
        }

        if (loginLogica != null) {
            loginLogica.logout();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login - Sistema Hospital");
            stage.show();
        } catch (Exception e) {
            Alerta.error("Error", "Error al regresar al login");
        }
    }

    private void mostrarCargando(boolean mostrar) {
        if (progressIndicator != null) {
            Platform.runLater(() -> progressIndicator.setVisible(mostrar));
        }
    }

    private static class DatosGraficos {
        final Map<String, Integer> datosLineas;
        final Map<String, Long> datosPastel;

        DatosGraficos(Map<String, Integer> datosLineas, Map<String, Long> datosPastel) {
            this.datosLineas = datosLineas;
            this.datosPastel = datosPastel;
        }
    }

}
