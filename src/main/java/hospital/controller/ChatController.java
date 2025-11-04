package hospital.controller;

import hospital.logica.Sesion;
import hospital.servicios.HospitalClient;
import hospital.servicios.ServiceProxy;
import hospital.servicios.ServiceProxy.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatController {
    @FXML private ListView<UsuarioActivo> lstUsuariosActivos;
    @FXML private TextArea txtAreaMensajes;
    @FXML private TextField txtMensaje;
    @FXML private Button btnEnviar;
    @FXML private Button btnActualizar;
    @FXML private Button btnCerrar;
    @FXML private Label lblUsuarioActual;
    @FXML private ProgressIndicator progressIndicator;

    private final ServiceProxy serviceProxy = ServiceProxy.getInstance();
    private ObservableList<UsuarioActivo> usuariosActivos;

    @FXML
    public void initialize() {
        usuariosActivos = FXCollections.observableArrayList();
        lstUsuariosActivos.setItems(usuariosActivos);

        configurarListaUsuarios();

        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }

        if (Sesion.getUsuario() != null) {
            lblUsuarioActual.setText("Usuario: " + Sesion.getUsuario().getNombre());
        }
        configurarListenerMensajes();

        cargarUsuariosActivos();

        agregarMensajeSistema("Conectado al sistema de mensajería");
    }

        private void configurarListaUsuarios() {
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
            lstUsuariosActivos.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> {
                        if (newVal != null) {
                            cargarHistorialConversacion(newVal.getId());
                        }
                    }
            );
        }

        private void cargarHistorialConversacion(String otroUsuarioId) {
            HospitalClient client = HospitalClient.getInstance();

            client.cargarHistorial(otroUsuarioId, respuesta -> {
                Platform.runLater(() -> {
                    txtAreaMensajes.clear();

                    if (respuesta.startsWith("OK")) {
                        String[] partes = respuesta.split("\\|");

                        if (partes.length > 1) {
                            for (int i = 1; i < partes.length; i++) {
                                String[] datos = partes[i].split(",", 3);
                                if (datos.length >= 3) {
                                    String remitenteId = datos[0];
                                    String mensaje = datos[1];
                                    String fechaStr = datos[2];

                                    try {
                                        LocalDateTime fecha = LocalDateTime.parse(fechaStr);
                                        String timestamp = fecha.format(
                                                DateTimeFormatter.ofPattern("HH:mm:ss")
                                        );

                                        String remitente = remitenteId.equals(Sesion.getUsuario().getId())
                                                ? "Tú" : remitenteId;

                                        txtAreaMensajes.appendText(
                                                String.format("[%s] %s: %s\n", timestamp, remitente, mensaje)
                                        );
                                    } catch (Exception e) {
                                        System.err.println("Error parseando fecha: " + e.getMessage());
                                    }
                                }
                            }
                        } else {
                            txtAreaMensajes.appendText("--- Sin mensajes previos ---\n");
                        }
                    }
                });
            });
        }

    private void configurarListenerMensajes() {
        serviceProxy.setOnMensajeRecibido(mensaje -> {
            switch (mensaje.getTipo()) {
                case MENSAJE:
                    mostrarMensajeRecibido(
                            mensaje.getRemitente(),
                            mensaje.getContenido()
                    );
                    break;

                case USUARIO_CONECTADO:
                    agregarMensajeSistema(mensaje.getContenido());
                    cargarUsuariosActivos();
                    break;

                case USUARIO_DESCONECTADO:
                    agregarMensajeSistema(mensaje.getContenido());
                    cargarUsuariosActivos();
                    break;

                case ERROR:
                    Alerta.error("Error", mensaje.getContenido());
                    break;
            }
        });
    }

    private void mostrarMensajeRecibido(String remitente, String mensaje) {
        String timestamp = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
        );

        txtAreaMensajes.appendText(String.format("[%s] %s: %s\n",
                timestamp, remitente, mensaje));

        Alerta.mostrarNotificacionMensaje(remitente, mensaje);
    }

    private void agregarMensajeSistema(String mensaje) {
        String timestamp = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
        );

        txtAreaMensajes.appendText(String.format("[%s] SISTEMA: %s\n",
                timestamp, mensaje));
    }

    @FXML
    private void cargarUsuariosActivos() {
        if (!serviceProxy.isConectado()) {
            Alerta.error("Error", "No hay conexión con el servidor");
            return;
        }

        mostrarCargando(true);
        deshabilitarControles(true);

        serviceProxy.listarUsuariosActivos(
                // OnSuccess
                usuarios -> {
                    usuariosActivos.clear();
                    usuariosActivos.addAll(usuarios);

                    mostrarCargando(false);
                    deshabilitarControles(false);

                    if (usuarios.isEmpty()) {
                        agregarMensajeSistema("No hay otros usuarios conectados");
                        Alerta.info("Información", "Actualmente no hay otros usuarios conectados.");
                    }
                },
                // OnError
                error -> {
                    mostrarCargando(false);
                    deshabilitarControles(false);
                    Alerta.error("Error", error);
                }
        );
    }

    @FXML
    private void enviarMensaje() {
        UsuarioActivo destinatario = lstUsuariosActivos.getSelectionModel().getSelectedItem();

        if (destinatario == null) {
            Alerta.advertencia("Advertencia", "Debe seleccionar un usuario de la lista");
            return;
        }

        String mensaje = txtMensaje.getText().trim();

        if (mensaje.isEmpty()) {
            Alerta.advertencia("Advertencia", "El mensaje no puede estar vacío");
            return;
        }

        if (!serviceProxy.isConectado()) {
            Alerta.error("Error", "No hay conexión con el servidor");
            return;
        }

        deshabilitarControles(true);
        mostrarCargando(true);

        serviceProxy.enviarMensaje(destinatario.getId(), mensaje, exito -> {
            mostrarCargando(false);
            deshabilitarControles(false);

            if (exito) {
                String timestamp = java.time.LocalTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
                );
                txtAreaMensajes.appendText(String.format("[%s] Tú → %s: %s\n",
                        timestamp,
                        destinatario.getNombre(),
                        mensaje));

                txtMensaje.clear();
            } else {
                Alerta.error("Error", "No se pudo enviar el mensaje");
            }

            txtMensaje.requestFocus();
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
}