package hospital.controller;

import hospital.Intermediaria.DashboardIntermediaria;
import hospital.Intermediaria.LoginIntermediaria;
import hospital.logica.Sesion;
import hospital.logica.UsuarioManager;
import hospital.model.Medico;
import hospital.model.Usuario;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.Pane;
import java.io.IOException;
import java.time.YearMonth;

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

    private final DashboardIntermediaria dashboardIntermediaria = new DashboardIntermediaria();
    private final UsuarioManager usuarioManager = new UsuarioManager();
    private LoginIntermediaria loginIntermediaria;

    public void setLoginController(LoginIntermediaria loginIntermediaria) {
        this.loginIntermediaria = loginIntermediaria;
    }

    public LoginIntermediaria getLoginController() {
        return loginIntermediaria;
    }

    @FXML
    public void initialize() {
        Usuario usuario = Sesion.getUsuario();
        if (usuario != null && lblUsuario != null) {
            lblUsuario.setText(usuario.getNombre());
        }
        configurarPermisosPorRol(usuario);
        mostrarGraficoLineas(usuario);
        mostrarGraficoPastel(usuario);
    }
    private void configurarPermisosPorRol(Usuario usuario) {
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
        // Deshabilitar funciones no permitidas
        btnMedicos.setDisable(true);
        btnFarmaceutas.setDisable(true);
        btnPacientes.setDisable(true);
        btnMedicamentos.setDisable(true);
        btnDespachoReceta.setDisable(true);

        // Habilitar funciones permitidas
        btnPrescribirReceta.setDisable(false);
        btnHistoricoRecetas.setDisable(false);
        btnCambiarClave.setDisable(false);
        btnAcercaDe.setDisable(false);
        btnLogout.setDisable(false);
    }

    private void configurarPermisosFarmaceuta() {
        // Deshabilitar funciones no permitidas
        btnMedicos.setDisable(true);
        btnFarmaceutas.setDisable(true);
        btnPacientes.setDisable(true);
        btnMedicamentos.setDisable(true);
        btnPrescribirReceta.setDisable(true);

        // Habilitar funciones permitidas
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

        // Funciones básicas siempre deshabilitadas si no hay usuario reconocido
        btnHistoricoRecetas.setDisable(true);
        btnCambiarClave.setDisable(true);
        btnAcercaDe.setDisable(true);
        btnLogout.setDisable(true);
    }

    private void mostrarGraficoLineas(Usuario usuario) {
        if (usuario == null) return;
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Medicamentos por mes");
        lineChart.setPrefWidth(350);
        lineChart.setPrefHeight(250);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Medicamentos");

        try {
            var datos = dashboardIntermediaria.contarMedicamentosPorMes(
                    usuario,
                    YearMonth.now().minusMonths(5),
                    YearMonth.now()
            );
            datos.forEach((mes, cantidad) -> series.getData().add(new XYChart.Data<>(mes, cantidad)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        lineChart.getData().add(series);
        paneLineChart.getChildren().clear();
        paneLineChart.getChildren().add(lineChart);
    }

    private void mostrarGraficoPastel(Usuario usuario) {
        if (usuario == null) return;
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Recetas por estado");
        pieChart.setPrefWidth(350);
        pieChart.setPrefHeight(250);

        try {
            var datos = dashboardIntermediaria.contarRecetasPorEstado(usuario);
            datos.forEach((estado, cantidad) ->
                    pieChart.getData().add(new PieChart.Data(estado, cantidad))
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        panePieChart.getChildren().clear();
        panePieChart.getChildren().add(pieChart);
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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("pacientesAdmin.fxml"));
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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("medicamentosAdmin.fxml"));
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

            // Verificar que el usuario sea realmente un médico
            UsuarioManager.TipoUsuario tipo = usuarioManager.determinarTipoUsuario(usuarioActual.getId());
            if (tipo != UsuarioManager.TipoUsuario.MEDICO) {
                Alerta.error("Acceso denegado", "Solo los médicos pueden prescribir recetas.");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/prescribirReceta.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Obtener el controlador y configurar el médico
            PreescribirRecetaController controller = fxmlLoader.getController();

            // Crear objeto Medico con los datos del usuario autenticado
            Medico medico = new Medico();
            medico.setId(usuarioActual.getId());
            medico.setNombre(usuarioActual.getNombre());
            // Agregar otros campos necesarios según tu modelo Medico

            controller.setMedico(medico);

            Stage stage = (Stage) btnPrescribirReceta.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Prescribir Receta");
            stage.show();
        } catch (IOException e) {
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
            cambioClaveController.setLoginController(loginIntermediaria);

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
    public void logout(){
        if (loginIntermediaria != null) {
            loginIntermediaria.logout();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login - Sistema Hospital");
            stage.show();
        } catch (IOException e) {
           Alerta.error("Error","Error al regresar al login");
        }
    }
}
