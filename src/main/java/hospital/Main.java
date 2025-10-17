package hospital;

import hospital.controller.busqueda.Async;
import hospital.logica.AdministradorLogica;
import hospital.model.Administrador;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public void start(Stage primaryStage) throws Exception {
        // Usuario: admin Clave: admin
        AdministradorLogica adminLogica = new AdministradorLogica();
        if (adminLogica.buscarPorId("admin") == null) {
            Administrador superAdmin = new Administrador();
            superAdmin.setId("admin");
            superAdmin.setNombre("Atenea");
            adminLogica.agregar(superAdmin);
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/login.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Login Hospital");
        primaryStage.setScene(scene);
        // Esta linea maneja el cierre de la aplicación
        primaryStage.setOnCloseRequest(event -> {
            Async.cerrarConexion();
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}