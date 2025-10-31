package hospital.servicios;

import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class HospitalClient {
    private static HospitalClient instance;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final AtomicBoolean conectado = new AtomicBoolean(false);
    private final AtomicBoolean reconectando = new AtomicBoolean(false);

    private final Map<Integer, Consumer<String>> callbacks = new ConcurrentHashMap<>();
    private final AtomicInteger requestId = new AtomicInteger(0);

    private Consumer<String> onMensajeRecibido;
    private Consumer<Boolean> onEstadoConexion;

    private Thread listenerThread;
    private Thread watchdogThread; // thread para detectar desconexiones

    private String lastHost;
    private int lastPort;
    private int intentosReconexion = 0;
    private final int MAX_INTENTOS_RECONEXION = 3;
    private final long DELAY_RECONEXION_MS = 3000;

    private final Map<String, Consumer<String>> callbacksPendientes = new ConcurrentHashMap<>();

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
        if (conectado.get()) {
            System.out.println("Ya hay una conexión activa");
            return;
        }

        this.lastHost = host;
        this.lastPort = port;

        try {
            socket = new Socket(host, port);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            conectado.set(true);
            intentosReconexion = 0;

            iniciarListener();
            iniciarWatchdog();

            System.out.println("✓ Conectado al servidor: " + host + ":" + port);
            notificarEstadoConexion(true);

        } catch (IOException e) {
            conectado.set(false);
            notificarEstadoConexion(false);
            throw new IOException("No se pudo conectar al servidor: " + e.getMessage(), e);
        }
    }

    private void iniciarListener() {
        listenerThread = new Thread(() -> {
            try {
                String linea;
                while (conectado.get() && (linea = in.readLine()) != null) {
                    final String mensaje = linea;
                    System.out.println("← Recibido: " + mensaje);
                    procesarMensaje(mensaje);
                }
            } catch (SocketException e) {
                if (conectado.get()) {
                    System.err.println("✗ Conexión perdida con el servidor");
                    manejarDesconexion();
                }
            } catch (IOException e) {
                if (conectado.get()) {
                    System.err.println("✗ Error en listener: " + e.getMessage());
                    manejarDesconexion();
                }
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.setName("HospitalClient-Listener");
        listenerThread.start();
    }

    private void iniciarWatchdog() {
        watchdogThread = new Thread(() -> {
            while (conectado.get()) {
                try {
                    Thread.sleep(30000); // Verificar cada 30 segundos

                    if (!verificarConexion()) {
                        System.err.println("✗ Watchdog: Conexión no responde");
                        manejarDesconexion();
                        break;
                    }

                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        watchdogThread.setDaemon(true);
        watchdogThread.setName("HospitalClient-Watchdog");
        watchdogThread.start();
    }

    private boolean verificarConexion() {
        if (socket == null || socket.isClosed()) {
            return false;
        }

        try {
            out.println("PING");
            return !out.checkError();
        } catch (Exception e) {
            return false;
        }
    }

    private void manejarDesconexion() {
        if (reconectando.get()) {
            return;
        }

        conectado.set(false);
        notificarEstadoConexion(false);

        System.out.println("Intentando reconexión...");

        Platform.runLater(() -> {
            if (onMensajeRecibido != null) {
                onMensajeRecibido.accept("ERROR|Conexión perdida con el servidor");
            }
        });

        // Intentar reconexión en segundo plano
        new Thread(this::intentarReconexion).start();
    }

    private void intentarReconexion() {
        if (reconectando.getAndSet(true)) {
            return;
        }

        cerrarRecursos();

        while (intentosReconexion < MAX_INTENTOS_RECONEXION) {
            intentosReconexion++;

            System.out.println("Intento de reconexión " + intentosReconexion + "/" + MAX_INTENTOS_RECONEXION);

            try {
                Thread.sleep(DELAY_RECONEXION_MS);
                conectar(lastHost, lastPort);

                System.out.println(" Reconexión exitosa");
                reconectando.set(false);

                Platform.runLater(() -> {
                    if (onMensajeRecibido != null) {
                        onMensajeRecibido.accept("NOTIFICACION|RECONEXION|Conexión restablecida");
                    }
                });

                return;

            } catch (Exception e) {
                System.err.println(" Reconexión fallida: " + e.getMessage());
            }
        }

        // Reconexión fallida después de todos los intentos
        reconectando.set(false);
        System.err.println("✗ No se pudo reconectar después de " + MAX_INTENTOS_RECONEXION + " intentos");

        Platform.runLater(() -> {
            if (onMensajeRecibido != null) {
                onMensajeRecibido.accept("ERROR|No se pudo reconectar al servidor. Reinicie la aplicación.");
            }
        });
    }

    private void procesarMensaje(String mensaje) {
        if (mensaje == null || mensaje.isEmpty()) {
            return;
        }

        String[] partes = mensaje.split("\\|", 2);
        String tipo = partes[0];

        if ("PONG".equals(tipo)) {
            return;
        }

        if (callbacksPendientes.containsKey("LOGIN")) {
            if ("OK".equals(tipo) || "ERROR".equals(tipo)) {
                Consumer<String> callback = callbacksPendientes.remove("LOGIN");
                if (callback != null) {
                    final String msg = mensaje;
                    Platform.runLater(() -> callback.accept(msg));
                }
                return;
            }
        }

        for (String comando : callbacksPendientes.keySet()) {
            if (mensaje.startsWith("OK") || mensaje.startsWith("ERROR")) {
                Consumer<String> callback = callbacksPendientes.remove(comando);
                if (callback != null) {
                    final String msg = mensaje;
                    Platform.runLater(() -> callback.accept(msg));
                    return;
                }
            }
        }
        if ("NOTIFICACION".equals(tipo) || "MENSAJE".equals(tipo)) {
            if (onMensajeRecibido != null) {
                Platform.runLater(() -> onMensajeRecibido.accept(mensaje));
            }
        } else {
            // Otros mensajes generales
            if (onMensajeRecibido != null) {
                Platform.runLater(() -> onMensajeRecibido.accept(mensaje));
            }
        }
    }

    /*public String enviarComandoSync(String comando) throws IOException {
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
*/
    public void enviarComando(String comando, Consumer<String> callback) {
        if (!conectado.get()) {
            if (callback != null) {
                Platform.runLater(() -> callback.accept("ERROR|No hay conexión con el servidor"));
            }
            return;
        }

        // Registrar el callback con el tipo de comando
        String tipoComando = comando.split("\\|")[0];
        if (callback != null) {
            callbacksPendientes.put(tipoComando, callback);
        }

        new Thread(() -> {
            try {
                System.out.println("→ Enviando: " + comando);
                out.println(comando);

                if (out.checkError()) {
                    callbacksPendientes.remove(tipoComando);
                    throw new IOException("Error al enviar comando");
                }

            } catch (IOException e) {
                System.err.println("✗ Error enviando comando: " + e.getMessage());
                callbacksPendientes.remove(tipoComando);
                manejarDesconexion();

                if (callback != null) {
                    Platform.runLater(() ->
                            callback.accept("ERROR|Error de comunicación: " + e.getMessage())
                    );
                }
            }
        }).start();
    }

    public void setOnMensajeRecibido(Consumer<String> listener) {
        this.onMensajeRecibido = listener;
    }

    public void setOnEstadoConexion(Consumer<Boolean> listener) {
        this.onEstadoConexion = listener;
    }

    private void notificarEstadoConexion(boolean conectado) {
        if (onEstadoConexion != null) {
            Platform.runLater(() -> onEstadoConexion.accept(conectado));
        }
    }
    private void cerrarRecursos() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Error cerrando recursos: " + e.getMessage());
        }
    }

    public void desconectar() {
        conectado.set(false);
        reconectando.set(false);

        if (watchdogThread != null && watchdogThread.isAlive()) {
            watchdogThread.interrupt();
        }

        cerrarRecursos();

        System.out.println("Desconectado del servidor");
        notificarEstadoConexion(false);
    }

    public boolean isConectado() {
        return conectado.get() && socket != null && !socket.isClosed();
    }

    public void login(String usuario, String clave, Consumer<String> callback) {
        String comando = "LOGIN|" + usuario + "|" + clave;
        enviarComando(comando, callback);
    }

    public void logout(Consumer<String> callback) {
        enviarComando("LOGOUT", callback);
    }

//    public void listarMedicos(Consumer<String> callback) {
//        enviarComando("LISTAR_MEDICOS", callback);
//    }
//
//    public void listarPacientes(Consumer<String> callback) {
//        enviarComando("LISTAR_PACIENTES", callback);
//    }
//
//    public void listarRecetas(Consumer<String> callback) {
//        enviarComando("LISTAR_RECETAS", callback);
//    }
//
//    public void actualizarEstadoReceta(String recetaId, String nuevoEstado, Consumer<String> callback) {
//        String comando = "ACTUALIZAR_ESTADO_RECETA|" + recetaId + "|" + nuevoEstado;
//        enviarComando(comando, callback);
//    }
//
//    public void enviarMensajePrivado(String destinatarioId, String mensaje, Consumer<String> callback) {
//        String comando = "ENVIAR_MENSAJE|" + destinatarioId + "|" + mensaje;
//        enviarComando(comando, callback);
//    }
//
//    public void listarUsuariosActivos(Consumer<String> callback) {
//        enviarComando("LISTAR_USUARIOS_ACTIVOS", callback);
//    }
}
