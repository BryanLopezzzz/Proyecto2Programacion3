package hospital.controller;

import hospital.logica.Sesion;
import hospital.servicios.HospitalClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ChatController { //Nueva clase para el Frontend
    @FXML
    private ListView<UsuarioActivo> lstUsuariosActivos;

    @FXML
    private TextArea txtAreaMensajes;

    @FXML
    private TextField txtMensaje;

    @FXML
    private Button btnEnviar;

    @FXML
    private Button btnActualizar;

    @FXML
    private Button btnCerrar;

    @FXML
    private Label lblUsuarioActual;

    @FXML
    private ProgressIndicator progressIndicator;

    private HospitalClient client;
    private ObservableList<UsuarioActivo> usuariosActivos;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usuariosActivos = FXCollections.observableArrayList();
        lstUsuariosActivos.setItems(usuariosActivos);

        // Configurar la celda personalizada para mostrar usuarios
        lstUsuariosActivos.setCellFactory(param -> new ListCell<UsuarioActivo>() {
            @Override
            protected void updateItem(UsuarioActivo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getNombre() + " (" + item.getRol() + ")");
                    setStyle("-fx-padding: 5px;");
                }
            }
        });

        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }

        // Mostrar usuario actual
        if (Sesion.getUsuario() != null) {
            lblUsuarioActual.setText("Usuario: " + Sesion.getUsuario().getNombre());
        }

        // Inicializar el cliente
        inicializarCliente();
    }

    private void inicializarCliente() {
        try {
            // Crear instancia del cliente
            client = HospitalClient.getInstance();

            // Verificar si ya está conectado
            if (!client.isConectado()) {
                Alerta.error("Error", "No hay conexión con el servidor. " +
                        "Por favor, reinicie la aplicación.");
                return;
            }

            // Configurar el listener de mensajes
            client.setOnMensajeRecibido(this::procesarMensajeRecibido);

            // Cargar usuarios activos
            cargarUsuariosActivos();

            agregarMensajeSistema("Conectado al sistema de mensajería");

        } catch (Exception e) {
            Alerta.error("Error", "Error al inicializar el chat: " + e.getMessage());
        }
    }

    private void procesarMensajeRecibido(String mensaje) {
        Platform.runLater(() -> {
            try {
                String[] partes = mensaje.split("\\|", -1);
                String tipo = partes[0];

                switch (tipo) {
                    case "MENSAJE":
                        // Formato: MENSAJE|remitenteId|remitenteNombre|texto
                        if (partes.length >= 4) {
                            String remitenteNombre = partes[2];
                            String texto = partes[3];
                            mostrarMensajeRecibido(remitenteNombre, texto);
                        }
                        break;

                    case "NOTIFICACION":
                        // Formato: NOTIFICACION|LOGIN/LOGOUT|usuarioId|nombre|rol
                        if (partes.length >= 5) {
                            String accion = partes[1];
                            String usuarioNombre = partes[3];
                            String rol = partes[4];

                            if ("LOGIN".equals(accion)) {
                                agregarMensajeSistema(usuarioNombre + " se ha conectado");
                                cargarUsuariosActivos();
                            } else if ("LOGOUT".equals(accion)) {
                                agregarMensajeSistema(usuarioNombre + " se ha desconectado");
                                cargarUsuariosActivos();
                            }
                        }
                        break;

                    case "ERROR":
                        if (partes.length >= 2) {
                            Alerta.error("Error", partes[1]);
                        }
                        break;
                }

            } catch (Exception e) {
                System.err.println("Error procesando mensaje: " + e.getMessage());
            }
        });
    }

    private void mostrarMensajeRecibido(String remitente, String mensaje) {
        String timestamp = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
        );

        txtAreaMensajes.appendText(String.format("[%s] %s: %s\n",
                timestamp, remitente, mensaje));

        // Mostrar notificación emergente
        mostrarNotificacionMensaje(remitente, mensaje);
    }

    private void agregarMensajeSistema(String mensaje) {
        String timestamp = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
        );

        txtAreaMensajes.appendText(String.format("[%s] SISTEMA: %s\n",
                timestamp, mensaje));
    }

    private void mostrarNotificacionMensaje(String remitente, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Nuevo Mensaje");
        alert.setHeaderText("Mensaje de: " + remitente);
        alert.setContentText(mensaje);

        // Hacer que la alerta sea no modal y se cierre automáticamente
        alert.show();
    }

    @FXML
    private void cargarUsuariosActivos() {
        if (client == null || !client.isConectado()) {
            Alerta.error("Error", "No hay conexión con el servidor");
            return;
        }

        mostrarCargando(true);
        deshabilitarControles(true);

        // Enviar petición al servidor
        client.enviarComando("LISTAR_USUARIOS_ACTIVOS", respuesta -> {
            Platform.runLater(() -> {
                try {
                    String[] partes = respuesta.split("\\|");

                    if ("OK".equals(partes[0])) {
                        usuariosActivos.clear();

                        // Parsear usuarios
                        for (int i = 1; i < partes.length; i++) {
                            String[] datos = partes[i].split(",");
                            if (datos.length >= 3) {
                                String id = datos[0];
                                String nombre = datos[1];
                                String rol = datos[2];

                                usuariosActivos.add(new UsuarioActivo(id, nombre, rol));
                            }
                        }

                        if (usuariosActivos.isEmpty()) {
                            agregarMensajeSistema("No hay otros usuarios conectados");
                        }
                    } else {
                        Alerta.error("Error", "Error al cargar usuarios: " + partes[1]);
                    }

                } catch (Exception e) {
                    Alerta.error("Error", "Error procesando usuarios: " + e.getMessage());
                } finally {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                }
            });
        });
    }

    @FXML
    private void enviarMensaje() {
        UsuarioActivo destinatario = lstUsuariosActivos.getSelectionModel().getSelectedItem();

        if (destinatario == null) {
            Alerta.error("Error", "Debe seleccionar un usuario de la lista");
            return;
        }

        String mensaje = txtMensaje.getText().trim();

        if (mensaje.isEmpty()) {
            Alerta.error("Error", "El mensaje no puede estar vacío");
            return;
        }

        if (client == null || !client.isConectado()) {
            Alerta.error("Error", "No hay conexión con el servidor");
            return;
        }

        // Deshabilitar controles mientras se envía
        deshabilitarControles(true);
        mostrarCargando(true);

        // Enviar mensaje
        String comando = "ENVIAR_MENSAJE|" + destinatario.getId() + "|" + mensaje;

        client.enviarComando(comando, respuesta -> {
            Platform.runLater(() -> {
                try {
                    String[] partes = respuesta.split("\\|");

                    if ("OK".equals(partes[0])) {
                        // Mostrar mensaje enviado
                        String timestamp = java.time.LocalTime.now().format(
                                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
                        );
                        txtAreaMensajes.appendText(String.format("[%s] Tú → %s: %s\n",
                                timestamp,
                                destinatario.getNombre(),
                                mensaje));

                        // Limpiar campo de texto
                        txtMensaje.clear();
                    } else {
                        Alerta.error("Error", "Error al enviar mensaje: " +
                                (partes.length > 1 ? partes[1] : "Desconocido"));
                    }

                } catch (Exception e) {
                    Alerta.error("Error", "Error procesando respuesta: " + e.getMessage());
                } finally {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    txtMensaje.requestFocus();
                }
            });
        });
    }

    @FXML
    private void actualizarLista() {
        cargarUsuariosActivos();
    }

    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }

    private void deshabilitarControles(boolean deshabilitar) {
        btnEnviar.setDisable(deshabilitar);
        btnActualizar.setDisable(deshabilitar);
        txtMensaje.setDisable(deshabilitar);
        lstUsuariosActivos.setDisable(deshabilitar);
    }

    private void mostrarCargando(boolean mostrar) {
        if (progressIndicator != null) {
            progressIndicator.setVisible(mostrar);
        }
    }

    public static class UsuarioActivo {
        private final String id;
        private final String nombre;
        private final String rol;

        public UsuarioActivo(String id, String nombre, String rol) {
            this.id = id;
            this.nombre = nombre;
            this.rol = rol;
        }

        public String getId() { return id; }
        public String getNombre() { return nombre; }
        public String getRol() { return rol; }

        @Override
        public String toString() {
            return nombre + " (" + rol + ")";
        }
    }
}
