package hospital.servicios;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.*;

public class HospitalServer {
    private final int port;
    private final Set<ClientHandler> clientes = Collections.synchronizedSet(new HashSet<>());
    private static final Logger LOGGER = Logger.getLogger(HospitalServer.class.getName());

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
            LOGGER.setLevel(Level.INFO);
        } catch (IOException e) {
            System.err.println("No se ha podido generar la bitácora del servidor");
        }
    }

    public void start() {
        LOGGER.info("Iniciando el servidor en el puerto: " + port);
        System.out.println("=== Servidor Hospital ===");
        System.out.println("Puerto: " + port);
        System.out.println("Esperando conexiones...\n");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket, this);
                clientes.add(clientHandler);
                clientHandler.start();
                LOGGER.info("Conexión aceptada desde: " + socket.getRemoteSocketAddress());
                System.out.println("Cliente conectado: " + socket.getRemoteSocketAddress());
            }

        } catch (IOException ex) {
            LOGGER.severe("Error en el servidor: " + ex.getMessage());
        }
    }

    //Esto envia un mensaje a todos los clientes conectados
    public void broadcast(String mensaje, ClientHandler remitente) {
        synchronized (clientes) {
            for (ClientHandler cliente : clientes) {
                if (cliente != remitente) {
                    cliente.send(mensaje);
                }
            }
        }
    }

    public void remove(ClientHandler clientHandler) {
        clientes.remove(clientHandler);
        LOGGER.info("Cliente eliminado de la conexión: " + clientHandler.getName());
        System.out.println("✗ Cliente desconectado. Activos: " + clientes.size());
    }

   //Esto retorna el número de clientes conectados
    public int getClientesActivos() {
        return clientes.size();
    }
}