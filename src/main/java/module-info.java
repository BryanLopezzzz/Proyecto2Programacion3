module ProyectoProgramacion3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires jakarta.xml.bind;
    requires javafx.base;
    requires javafx.graphics;
    requires com.zaxxer.hikari;
    requires com.mysql.cj;
    requires java.sql;

    // JavaFX
    opens hospital.controller to javafx.fxml;
    opens hospital.controller.busqueda to javafx.fxml;
    opens hospital.controller.registro to javafx.fxml;


    // JAXB
    opens hospital.datos.conector to jakarta.xml.bind;
    opens hospital.datos.entidades to jakarta.xml.bind, org.glassfish.jaxb.runtime;
    opens hospital.model to jakarta.xml.bind;
    opens hospital.datos to org.glassfish.jaxb.core, org.glassfish.jaxb.runtime;

    opens icons to javafx.fxml, javafx.graphics;

    exports hospital;
    exports hospital.model;
    exports hospital.controller;
    exports hospital.controller.busqueda;
    exports hospital.logica;
    opens hospital.logica to javafx.fxml;
    opens hospital.datosDB to org.glassfish.jaxb.core, org.glassfish.jaxb.runtime;
}
