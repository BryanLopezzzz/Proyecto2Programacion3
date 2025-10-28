package hospital.servicios;
import hospital.controller.Alerta;
import hospital.model.*;
import javafx.application.Platform;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceProxy {
    private static ServiceProxy instance;
    private final HospitalClient client;
    private final Logger logger;
    private boolean modoOffline = false;

    private ServiceProxy() {
        this.client = HospitalClient.getInstance();
        this.logger = Logger.getLogger(getClass().getName());
    }

    public static synchronized ServiceProxy getInstance() {
        if (instance == null) {
            instance = new ServiceProxy();
        }
        return instance;
    }


    public void conectar(String host, int port, Consumer<Boolean> callback) {
        try {
            client.conectar(host, port);
            modoOffline = false;
            logger.info("Conectado al servidor: " + host + ":" + port);

            if (callback != null) {
                Platform.runLater(() -> callback.accept(true));
            }

        } catch (Exception e) {
            logger.warning("No se pudo conectar al servidor: " + e.getMessage());
            modoOffline = true;

            if (callback != null) {
                Platform.runLater(() -> callback.accept(false));
            }
        }
    }

    public boolean isConectado() {
        return client.isConectado() && !modoOffline;
    }

    public void desconectar() {
        client.desconectar();
    }

    // ==================== AUTENTICACIÓN ====================

    public void login(String usuario, String clave,
                      Consumer<LoginResponse> onSuccess,
                      Consumer<String> onError) {

        if (!validarConexion(onError)) return;

        client.login(usuario, clave, respuesta -> {
            Platform.runLater(() -> {
                try {
                    LoginResponse response = parsearLoginResponse(respuesta);

                    if (response.exito) {
                        logger.info("Login exitoso: " + response.nombre);
                        onSuccess.accept(response);
                    } else {
                        logger.warning("Login fallido: " + response.mensaje);
                        onError.accept(response.mensaje);
                    }

                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error procesando login", e);
                    onError.accept("Error al procesar respuesta del servidor");
                }
            });
        });
    }

    public void logout(Consumer<Boolean> callback) {
        if (!isConectado()) {
            if (callback != null) callback.accept(true);
            return;
        }

        client.logout(respuesta -> {
            Platform.runLater(() -> {
                boolean exito = respuesta.startsWith("OK");
                logger.info("Logout: " + (exito ? "exitoso" : "fallido"));

                if (callback != null) {
                    callback.accept(exito);
                }
            });
        });
    }

    // ==================== USUARIOS ACTIVOS ====================

    public void listarUsuariosActivos(
            Consumer<List<UsuarioActivo>> onSuccess,
            Consumer<String> onError) {

        if (!validarConexion(onError)) return;

        client.enviarComando("LISTAR_USUARIOS_ACTIVOS", respuesta -> {
            Platform.runLater(() -> {
                try {
                    List<UsuarioActivo> usuarios = parsearUsuariosActivos(respuesta);
                    onSuccess.accept(usuarios);

                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error parseando usuarios", e);
                    onError.accept("Error al procesar lista de usuarios");
                }
            });
        });
    }

    // ==================== MENSAJERÍA ====================

    public void enviarMensaje(String destinatarioId, String mensaje,
                              Consumer<Boolean> callback) {

        if (!validarConexion(error -> {
            if (callback != null) callback.accept(false);
        })) return;

        if (mensaje == null || mensaje.trim().isEmpty()) {
            if (callback != null) callback.accept(false);
            return;
        }

        client.enviarMensajePrivado(destinatarioId, mensaje, respuesta -> {
            Platform.runLater(() -> {
                boolean exito = respuesta.startsWith("OK");

                if (!exito) {
                    logger.warning("Error enviando mensaje: " + respuesta);
                }

                if (callback != null) {
                    callback.accept(exito);
                }
            });
        });
    }

    public void setOnMensajeRecibido(Consumer<MensajeRecibido> listener) {
        client.setOnMensajeRecibido(mensajeRaw -> {
            try {
                MensajeRecibido mensaje = parsearMensajeRecibido(mensajeRaw);

                if (mensaje != null) {
                    Platform.runLater(() -> listener.accept(mensaje));
                }

            } catch (Exception e) {
                logger.log(Level.WARNING, "Error parseando mensaje recibido", e);
            }
        });
    }

    // ==================== MÉDICOS ====================

    public void listarMedicos(
            Consumer<List<Medico>> onSuccess,
            Consumer<String> onError) {

        if (!validarConexion(onError)) return;

        client.listarMedicos(respuesta -> {
            Platform.runLater(() -> {
                try {
                    List<Medico> medicos = parsearListaMedicos(respuesta);
                    onSuccess.accept(medicos);

                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error parseando médicos", e);
                    onError.accept("Error al procesar lista de médicos");
                }
            });
        });
    }

    // ==================== RECETAS ====================

    public void listarRecetas(
            Consumer<List<Receta>> onSuccess,
            Consumer<String> onError) {

        if (!validarConexion(onError)) return;

        client.listarRecetas(respuesta -> {
            Platform.runLater(() -> {
                try {
                    List<Receta> recetas = parsearListaRecetas(respuesta);
                    onSuccess.accept(recetas);

                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error parseando recetas", e);
                    onError.accept("Error al procesar lista de recetas");
                }
            });
        });
    }

    public void actualizarEstadoReceta(String recetaId, EstadoReceta nuevoEstado,
                                       Consumer<Boolean> onSuccess,
                                       Consumer<String> onError) {

        if (!validarConexion(onError)) return;

        client.actualizarEstadoReceta(recetaId, nuevoEstado.name(), respuesta -> {
            Platform.runLater(() -> {
                if (respuesta.startsWith("OK")) {
                    logger.info("Estado actualizado: " + recetaId + " -> " + nuevoEstado);
                    onSuccess.accept(true);
                } else {
                    String error = extraerMensajeError(respuesta);
                    logger.warning("Error actualizando estado: " + error);
                    onError.accept(error);
                }
            });
        });
    }

    // ==================== PARSERS (Privados) ====================

    private LoginResponse parsearLoginResponse(String respuesta) {
        String[] partes = respuesta.split("\\|");

        if ("OK".equals(partes[0]) && partes.length >= 3) {
            return new LoginResponse(true, partes[1], partes[2], null);
        } else {
            String mensaje = partes.length > 1 ? partes[1] : "Error desconocido";
            return new LoginResponse(false, null, null, mensaje);
        }
    }

    private List<UsuarioActivo> parsearUsuariosActivos(String respuesta) {
        List<UsuarioActivo> usuarios = new ArrayList<>();
        String[] partes = respuesta.split("\\|");

        if ("OK".equals(partes[0])) {
            for (int i = 1; i < partes.length; i++) {
                String[] datos = partes[i].split(",");
                if (datos.length >= 3) {
                    usuarios.add(new UsuarioActivo(
                            datos[0], // id
                            datos[1], // nombre
                            datos[2]  // rol
                    ));
                }
            }
        }

        return usuarios;
    }

    private MensajeRecibido parsearMensajeRecibido(String mensajeRaw) {
        String[] partes = mensajeRaw.split("\\|", -1);
        String tipo = partes[0];

        switch (tipo) {
            case "MENSAJE":
                if (partes.length >= 4) {
                    return new MensajeRecibido(
                            TipoMensaje.MENSAJE,
                            partes[2], // remitente nombre
                            partes[3]  // texto
                    );
                }
                break;

            case "NOTIFICACION":
                if (partes.length >= 4) {
                    String accion = partes[1];
                    String usuarioNombre = partes[3];

                    if ("LOGIN".equals(accion)) {
                        return new MensajeRecibido(
                                TipoMensaje.USUARIO_CONECTADO,
                                usuarioNombre,
                                usuarioNombre + " se ha conectado"
                        );
                    } else if ("LOGOUT".equals(accion)) {
                        return new MensajeRecibido(
                                TipoMensaje.USUARIO_DESCONECTADO,
                                usuarioNombre,
                                usuarioNombre + " se ha desconectado"
                        );
                    }
                }
                break;

            case "ERROR":
                if (partes.length >= 2) {
                    return new MensajeRecibido(
                            TipoMensaje.ERROR,
                            "Sistema",
                            partes[1]
                    );
                }
                break;
        }

        return null;
    }

    private List<Medico> parsearListaMedicos(String respuesta) {
        List<Medico> medicos = new ArrayList<>();
        String[] partes = respuesta.split("\\|");

        if ("OK".equals(partes[0])) {
            for (int i = 1; i < partes.length; i++) {
                String[] datos = partes[i].split(",");
                if (datos.length >= 3) {
                    Medico medico = new Medico();
                    medico.setId(datos[0]);
                    medico.setNombre(datos[1]);
                    medico.setEspecialidad(datos[2]);
                    medicos.add(medico);
                }
            }
        }

        return medicos;
    }

    private List<Receta> parsearListaRecetas(String respuesta) {
        List<Receta> recetas = new ArrayList<>();
        String[] partes = respuesta.split("\\|");

        if ("OK".equals(partes[0])) {
            for (int i = 1; i < partes.length; i++) {
                String[] datos = partes[i].split(",");
                if (datos.length >= 5) {
                    Receta receta = new Receta();
                    receta.setId(datos[0]);
                    // Parsear más datos según necesites
                    recetas.add(receta);
                }
            }
        }

        return recetas;
    }

    // ==================== UTILIDADES ====================

    private boolean validarConexion(Consumer<String> onError) {
        if (!isConectado()) {
            if (onError != null) {
                Platform.runLater(() ->
                        onError.accept("No hay conexión con el servidor")
                );
            }
            return false;
        }
        return true;
    }

    private String extraerMensajeError(String respuesta) {
        if (respuesta.startsWith("ERROR|")) {
            String[] partes = respuesta.split("\\|", 2);
            return partes.length > 1 ? partes[1] : "Error desconocido";
        }
        return respuesta;
    }

    public static class LoginResponse {
        public final boolean exito;
        public final String nombre;
        public final String rol;
        public final String mensaje;

        public LoginResponse(boolean exito, String nombre, String rol, String mensaje) {
            this.exito = exito;
            this.nombre = nombre;
            this.rol = rol;
            this.mensaje = mensaje;
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
    }

    public static class MensajeRecibido {
        private final TipoMensaje tipo;
        private final String remitente;
        private final String contenido;

        public MensajeRecibido(TipoMensaje tipo, String remitente, String contenido) {
            this.tipo = tipo;
            this.remitente = remitente;
            this.contenido = contenido;
        }

        public TipoMensaje getTipo() { return tipo; }
        public String getRemitente() { return remitente; }
        public String getContenido() { return contenido; }
    }

    public enum TipoMensaje {
        MENSAJE,
        USUARIO_CONECTADO,
        USUARIO_DESCONECTADO,
        ERROR,
        NOTIFICACION
    }
}