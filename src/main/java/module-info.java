module ProyectoProgramacion3 {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.desktop;

    // Base de datos
    requires java.sql;
    requires com.zaxxer.hikari;
    opens hospital.controller to javafx.fxml;
    opens hospital.controller.busqueda to javafx.fxml;
    opens hospital.controller.registro to javafx.fxml;
    opens hospital.logica to javafx.fxml;
    opens icons to javafx.fxml, javafx.graphics;

    // Exportaciones
    exports hospital;
    exports hospital.model;
    exports hospital.controller;
    exports hospital.controller.busqueda;
    exports hospital.logica;
}
