package hospital.servicios;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.*;

public class HospitalServer {
    private final int port;
    private final Set<ClientHandler> clientes = Collections.synchronizedSet(new HashSet<>());
    private static final Logger LOGGER = Logger.getLogger(HospitalServer.class.getName());
    private volatile boolean running = true;
    private final Map<String, List<MensajeHistorial>> historialesMensajes = new ConcurrentHashMap<>();

    public HospitalServer(int port) {
        this.port = port;
        configureLogger();
    }

    public void configureLogger() {
        try {
            LOGGER.setUseParentHandlers(false);
            FileHandler fileHandler = new FileHandler("hospital-server.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(consoleHandler);
            LOGGER.setLevel(Level.INFO);
        } catch (IOException e) {
            System.err.println("No se ha podido generar la bitácora del servidor" + e.getMessage());
        }
    }

    public void start() {
        LOGGER.info("Iniciando el servidor en el puerto: " + port);
        System.out.println("=== Servidor Hospital ===");
        System.out.println("Puerto: " + port);
        System.out.println("Esperando conexiones...\n");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(socket, this);
                    clientes.add(clientHandler);
                    clientHandler.start();
                    LOGGER.info("Conexión aceptada desde: " + socket.getRemoteSocketAddress());
                    System.out.println("Cliente conectado: " + socket.getRemoteSocketAddress()
                            + " [Total activos: " + clientes.size() + "]");

            }catch(IOException ex) {
                    if (running) {
                        LOGGER.log(Level.WARNING, "Error aceptando conexión", ex);
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.severe("Error en el servidor: " + ex.getMessage());
            System.err.println("Error fatal en el servidor: " + ex.getMessage());
        }
    }

    public void guardarMensaje(String remitenteId, String destinatarioId, String mensaje) {
        String conversacionKey = generarKeyConversacion(remitenteId, destinatarioId);

        historialesMensajes.computeIfAbsent(conversacionKey, k -> new ArrayList<>())
                .add(new MensajeHistorial(remitenteId, destinatarioId, mensaje, LocalDateTime.now()));

        LOGGER.info("Mensaje guardado en historial: " + remitenteId + " -> " + destinatarioId);
    }

    public List<MensajeHistorial> obtenerHistorial(String usuario1, String usuario2) {
        String key = generarKeyConversacion(usuario1, usuario2);
        return new ArrayList<>(historialesMensajes.getOrDefault(key, new ArrayList<>()));
    }

    private String generarKeyConversacion(String usuario1, String usuario2) {
        // Ordenar alfabéticamente para que A-B y B-A sean la misma conversación
        return usuario1.compareTo(usuario2) < 0 ?
                usuario1 + ":" + usuario2 : usuario2 + ":" + usuario1;
    }


    public void notificarLogin(ClientHandler clienteQueIngresa) {
        if (clienteQueIngresa == null || !clienteQueIngresa.isAutenticado()) {
            return;
        }

        String mensaje = "NOTIFICACION|LOGIN|"
                + clienteQueIngresa.getUsuarioId() + "|"
                + clienteQueIngresa.getNombreUsuario() + "|"
                + clienteQueIngresa.getRol();

        LOGGER.info("Notificando login de: " + clienteQueIngresa.getNombreUsuario());
        System.out.println("BROADCAST: Usuario ingresó - " + clienteQueIngresa.getNombreUsuario()
                + " (" + clienteQueIngresa.getRol() + ")");

        // Enviar a todos excepto al que acaba de ingresar
        synchronized (clientes) {
            for (ClientHandler cliente : clientes) {
                if (cliente != clienteQueIngresa && cliente.isActivo()) {
                    cliente.send(mensaje);
                }
            }
        }
    }

    public boolean enviarMensajePrivado(ClientHandler remitente, String destinatarioId, String mensaje) {
        if (remitente == null || destinatarioId == null || mensaje == null) {
            return false;
        }

        guardarMensaje(remitente.getUsuarioId(), destinatarioId, mensaje);

        synchronized (clientes) {
            for (ClientHandler cliente : clientes) {
                if (cliente.isAutenticado()
                        && cliente.getUsuarioId().equals(destinatarioId)
                        && cliente.isActivo()) {

                    String mensajeFormateado = "MENSAJE|"
                            + remitente.getUsuarioId() + "|"
                            + remitente.getNombreUsuario() + "|"
                            + mensaje;

                    cliente.send(mensajeFormateado);

                    LOGGER.info("Mensaje enviado de " + remitente.getNombreUsuario()
                            + " a " + cliente.getNombreUsuario());
                    System.out.println("MENSAJE: " + remitente.getNombreUsuario()
                            + " → " + cliente.getNombreUsuario());

                    return true;
                }
            }
        }

        LOGGER.warning("Destinatario no encontrado o no conectado: " + destinatarioId);
        System.out.println("Destinatario offline, mensaje guardado para: " + destinatarioId);
        return true;
    }


    public void notificarLogout(ClientHandler clienteQueSale) {
        if (clienteQueSale == null) {
            return;
        }

        String mensaje = "NOTIFICACION|LOGOUT|"
                + clienteQueSale.getUsuarioId() + "|"
                + clienteQueSale.getNombreUsuario() + "|"
                + clienteQueSale.getRol();

        LOGGER.info("Notificando logout de: " + clienteQueSale.getNombreUsuario());
        System.out.println("BROADCAST: Usuario salió - " + clienteQueSale.getNombreUsuario());

        synchronized (clientes) {
            for (ClientHandler cliente : clientes) {
                if (cliente != clienteQueSale && cliente.isActivo()) {
                    cliente.send(mensaje);
                }
            }
        }
    }

    //Esto envia un mensaje a todos los clientes conectados
    public void broadcast(String mensaje, ClientHandler remitente) {
        if (mensaje == null) {
            return;
        }

        LOGGER.info("Broadcast: " + mensaje);
        System.out.println("BROADCAST: " + mensaje);

        synchronized (clientes) {
            for (ClientHandler cliente : clientes) {
                if (cliente != remitente && cliente.isActivo()) {
                    cliente.send(mensaje);
                }
            }
        }
    }

    public Set<ClientHandler> obtenerUsuariosActivos() {
        Set<ClientHandler> activos = new HashSet<>();

        synchronized (clientes) {
            for (ClientHandler cliente : clientes) {
                if (cliente.isAutenticado() && cliente.isActivo()) {
                    activos.add(cliente);
                }
            }
        }

        return activos;
    }

    public void remove(ClientHandler clientHandler) {
        if (clientHandler == null) {
            return;
        }

        clientes.remove(clientHandler);

        LOGGER.info("Cliente eliminado: " + clientHandler.getNombreUsuario()
                + " [Total activos: " + clientes.size() + "]");
        System.out.println("✗ Cliente desconectado: " + clientHandler.getNombreUsuario()
                + " [Activos: " + clientes.size() + "]");
    }

   //Esto retorna el número de clientes conectados
    public int getClientesActivos() {
        return clientes.size();
    }

    public int getUsuariosAutenticados() {
        int count = 0;
        synchronized (clientes) {
            for (ClientHandler cliente : clientes) {
                if (cliente.isAutenticado()) {
                    count++;
                }
            }
        }
        return count;
    }

    public void shutdown() {
        running = false;

        LOGGER.info("Apagando servidor...");
        System.out.println("\nApagando servidor...");

        synchronized (clientes) {
            for (ClientHandler cliente : clientes) {
                try {
                    cliente.send("NOTIFICACION|SERVER_SHUTDOWN|El servidor se está apagando");
                } catch (Exception e) {
                    // Ignorar errores al cerrar
                }
            }
            clientes.clear();
        }

        LOGGER.info("Servidor apagado");
        System.out.println("Servidor apagado correctamente");
    }

    public static class MensajeHistorial {
        private final String remitenteId;
        private final String destinatarioId;
        private final String mensaje;
        private final LocalDateTime fecha;

        public MensajeHistorial(String remitenteId, String destinatarioId, String mensaje, LocalDateTime fecha) {
            this.remitenteId = remitenteId;
            this.destinatarioId = destinatarioId;
            this.mensaje = mensaje;
            this.fecha = fecha;
        }

        public String getRemitenteId() { return remitenteId; }
        public String getDestinatarioId() { return destinatarioId; }
        public String getMensaje() { return mensaje; }
        public LocalDateTime getFecha() { return fecha; }

        @Override
        public String toString() {
            return String.format("[%s] %s → %s: %s",
                    fecha.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    remitenteId, destinatarioId, mensaje);
        }
    }
}