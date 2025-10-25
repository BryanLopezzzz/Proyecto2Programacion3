package hospital.servicios;

import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class HospitalClient {
    private static HospitalClient instance;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean conectado = false;

    private final Map<Integer, Consumer<String>> callbacks = new ConcurrentHashMap<>();
    private final AtomicInteger requestId = new AtomicInteger(0);

    private Consumer<String> onMensajeRecibido;

    private Thread listenerThread;

    private HospitalClient() {
        // Constructor privado para Singleton
    }

    public static synchronized HospitalClient getInstance() {
        if (instance == null) {
            instance = new HospitalClient();
        }
        return instance;
    }

    public void conectar(String host, int port) throws IOException {
        if (conectado) {
            return;
        }

        socket = new Socket(host, port);
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        conectado = true;

        iniciarListener();

        System.out.println("Conectado al servidor: " + host + ":" + port);
    }

    private void iniciarListener() {
        listenerThread = new Thread(() -> {
            try {
                String linea;
                while (conectado && (linea = in.readLine()) != null) {
                    final String mensaje = linea;
                    System.out.println("← Recibido: " + mensaje);

                    // Procesar el mensaje
                    procesarMensaje(mensaje);
                }
            } catch (IOException e) {
                if (conectado) {
                    System.err.println("Error en listener: " + e.getMessage());
                    desconectar();
                }
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.setName("HospitalClient-Listener");
        listenerThread.start();
    }

    private void procesarMensaje(String mensaje) {
        if (mensaje == null || mensaje.isEmpty()) {
            return;
        }

        String[] partes = mensaje.split("\\|", 2);
        String tipo = partes[0];

        // Verificar si es una notificación asíncrona
        if ("NOTIFICACION".equals(tipo) || "MENSAJE".equals(tipo)) {
            // Mensaje asíncrono (notificación o mensaje de chat)
            if (onMensajeRecibido != null) {
                Platform.runLater(() -> onMensajeRecibido.accept(mensaje));
            }
        } else {
            // Respuesta a un comando (puede tener callback pendiente)
            // Por ahora, enviamos todo al listener general
            if (onMensajeRecibido != null) {
                Platform.runLater(() -> onMensajeRecibido.accept(mensaje));
            }
        }
    }

    public String enviarComandoSync(String comando) throws IOException {
        if (!conectado) {
            throw new IOException("No hay conexión con el servidor");
        }

        System.out.println("→ Enviando: " + comando);
        out.println(comando);

        // Esperar respuesta
        String respuesta = in.readLine();
        System.out.println("← Respuesta: " + respuesta);

        return respuesta;
    }

    public void enviarComando(String comando, Consumer<String> callback) {
        if (!conectado) {
            if (callback != null) {
                Platform.runLater(() -> callback.accept("ERROR|No hay conexión con el servidor"));
            }
            return;
        }

        new Thread(() -> {
            try {
                System.out.println("→ Enviando: " + comando);
                out.println(comando);

                // Esperar respuesta
                String respuesta = in.readLine();
                System.out.println("← Respuesta: " + respuesta);

                if (callback != null) {
                    final String resp = respuesta;
                    Platform.runLater(() -> callback.accept(resp));
                }

            } catch (IOException e) {
                System.err.println("Error enviando comando: " + e.getMessage());
                if (callback != null) {
                    Platform.runLater(() -> callback.accept("ERROR|Error de comunicación: " + e.getMessage()));
                }
            }
        }).start();
    }

    public void setOnMensajeRecibido(Consumer<String> listener) {
        this.onMensajeRecibido = listener;
    }

    public void desconectar() {
        conectado = false;

        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();

            System.out.println("Desconectado del servidor");
        } catch (IOException e) {
            System.err.println("Error al desconectar: " + e.getMessage());
        }
    }

    public boolean isConectado() {
        return conectado && socket != null && !socket.isClosed();
    }

    public void login(String usuario, String clave, Consumer<String> callback) {
        String comando = "LOGIN|" + usuario + "|" + clave;
        enviarComando(comando, callback);
    }

    public void logout(Consumer<String> callback) {
        enviarComando("LOGOUT", callback);
    }

    public void listarMedicos(Consumer<String> callback) {
        enviarComando("LISTAR_MEDICOS", callback);
    }

    public void listarPacientes(Consumer<String> callback) {
        enviarComando("LISTAR_PACIENTES", callback);
    }

    public void listarRecetas(Consumer<String> callback) {
        enviarComando("LISTAR_RECETAS", callback);
    }

    public void actualizarEstadoReceta(String recetaId, String nuevoEstado, Consumer<String> callback) {
        String comando = "ACTUALIZAR_ESTADO_RECETA|" + recetaId + "|" + nuevoEstado;
        enviarComando(comando, callback);
    }

    public void enviarMensajePrivado(String destinatarioId, String mensaje, Consumer<String> callback) {
        String comando = "ENVIAR_MENSAJE|" + destinatarioId + "|" + mensaje;
        enviarComando(comando, callback);
    }

    public void listarUsuariosActivos(Consumer<String> callback) {
        enviarComando("LISTAR_USUARIOS_ACTIVOS", callback);
    }
}
