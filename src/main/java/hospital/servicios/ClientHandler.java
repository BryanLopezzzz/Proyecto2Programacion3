package hospital.servicios;

import hospital.logica.*;
import hospital.model.*;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler extends Thread { //Esta clase se puede simplificar con otras, pero necesito ver si el profe
    //hace algo más
    private final Socket socket;
    private final HospitalServer server;
    private PrintWriter out;
    private BufferedReader in;

    private String rol = "DESCONOCIDO";
    private String nombre = "anonimo";
    private String usuarioId = null;

    private static final Logger LOGGER = Logger.getLogger("HospitalServer");

    public ClientHandler(Socket socket, HospitalServer server) {
        this.socket = socket;
        this.server = server;
    }

    public void send(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            LOGGER.info("Cliente conectado: " + socket.getRemoteSocketAddress());
            send("BIENVENIDO|Sistema Hospital - Servidor Activo");

            String lineaRecibida;
            while ((lineaRecibida = in.readLine()) != null) {
                LOGGER.info("Recibido [" + nombre + "]: " + lineaRecibida);
                System.out.println("← [" + nombre + "]: " + lineaRecibida);

                String respuesta = procesarMensaje(lineaRecibida);

                send(respuesta);
                System.out.println("→ [" + nombre + "]: " + respuesta);
            }

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Conexión finalizada: " + e.getMessage());
        } finally {
            cerrarConexion();
        }
    }

    private String procesarMensaje(String mensaje) {
        try {
            String[] partes = mensaje.split("\\|");
            String comando = partes[0];

            switch (comando) {
                case "LOGIN":
                    return procesarLogin(partes);
                case "LOGOUT":
                    return procesarLogout();
                case "CAMBIAR_CLAVE":
                    return procesarCambiarClave(partes);
                case "LISTAR_MEDICOS":
                    return procesarListarMedicos();
                case "BUSCAR_MEDICO":
                    return procesarBuscarMedico(partes);
                case "AGREGAR_MEDICO":
                    return procesarAgregarMedico(partes);
                case "MODIFICAR_MEDICO":
                    return procesarModificarMedico(partes);
                case "ELIMINAR_MEDICO":
                    return procesarEliminarMedico(partes);
                case "LISTAR_PACIENTES":
                    return procesarListarPacientes();
                case "BUSCAR_PACIENTE":
                    return procesarBuscarPaciente(partes);
                case "AGREGAR_PACIENTE":
                    return procesarAgregarPaciente(partes);
                case "LISTAR_FARMACEUTAS":
                    return procesarListarFarmaceutas();
                case "LISTAR_MEDICAMENTOS":
                    return procesarListarMedicamentos();
                case "LISTAR_RECETAS":
                    return procesarListarRecetas();
                case "ACTUALIZAR_ESTADO_RECETA":
                    return procesarActualizarEstadoReceta(partes);
                case "PING":
                    return "PONG|Servidor activo";
                case "MENSAJE":
                    return procesarMensajeChat(partes);
                default:
                    return "ERROR|Comando desconocido: " + comando;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error procesando mensaje: " + e.getMessage(), e);
            return "ERROR|" + e.getMessage();
        }
    }

    // ==================== COMANDOS ====================

    private String procesarLogin(String[] partes) {
        if (partes.length < 3) {
            return "ERROR|Formato incorrecto. Uso: LOGIN|usuario|clave";
        }

        String id = partes[1];
        String clave = partes[2];

        try {
            LoginLogica loginLogica = new LoginLogica();
            Usuario usuario = loginLogica.login(id, clave);

            this.usuarioId = usuario.getId();
            this.nombre = usuario.getNombre();

            UsuarioManager usuarioManager = new UsuarioManager();
            UsuarioManager.TipoUsuario tipo = usuarioManager.determinarTipoUsuario(id);
            this.rol = tipo.name();

            LOGGER.info("Login exitoso: " + nombre + " (" + rol + ")");

            server.broadcast("NOTIFICACION|LOGIN|" + nombre + "|" + rol, this);

            return "OK|" + nombre + "|" + rol;

        } catch (Exception e) {
            LOGGER.warning("Login fallido para: " + id);
            return "ERROR|" + e.getMessage();
        }
    }

    private String procesarLogout() {
        if (usuarioId == null) {
            return "ERROR|No hay sesión activa";
        }

        String nombreAnterior = nombre;
        String rolAnterior = rol;

        server.broadcast("NOTIFICACION|LOGOUT|" + nombreAnterior + "|" + rolAnterior, this);

        usuarioId = null;
        nombre = "anonimo";
        rol = "DESCONOCIDO";

        return "OK|Logout exitoso";
    }

    private String procesarCambiarClave(String[] partes) {
        if (partes.length < 3) {
            return "ERROR|Formato: CAMBIAR_CLAVE|claveActual|nuevaClave";
        }

        if (usuarioId == null) {
            return "ERROR|Debe estar autenticado";
        }

        try {
            LoginLogica loginLogica = new LoginLogica();
            loginLogica.cambiarClave(partes[1], partes[2]);
            return "OK|Clave cambiada exitosamente";

        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }

    // ==================== MÉDICOS ====================

    private String procesarListarMedicos() {
        try {
            MedicoLogica medicoLogica = new MedicoLogica();
            var medicos = medicoLogica.listar(); // ← Usa la versión sin admin

            StringBuilder sb = new StringBuilder("OK");
            for (Medico m : medicos) {
                sb.append("|").append(m.getId())
                        .append(",").append(m.getNombre())
                        .append(",").append(m.getEspecialidad());
            }

            return sb.toString();

        } catch (SQLException e) { // ← Ahora captura SQLException específicamente
            LOGGER.log(Level.SEVERE, "Error listando médicos", e);
            return "ERROR|" + e.getMessage();
        }
    }

    private String procesarBuscarMedico(String[] partes) {
        if (partes.length < 2) {
            return "ERROR|Formato: BUSCAR_MEDICO|id";
        }

        try {
            MedicoLogica medicoLogica = new MedicoLogica();
            Medico m = medicoLogica.buscarPorId(partes[1]); // ← SQLException

            if (m == null) {
                return "ERROR|Médico no encontrado";
            }

            return "OK|" + m.getId() + "," + m.getNombre() + "," + m.getEspecialidad();

        } catch (SQLException e) {
            return "ERROR|" + e.getMessage();
        }
    }

    private String procesarAgregarMedico(String[] partes) {
        if (partes.length < 4) {
            return "ERROR|Formato: AGREGAR_MEDICO|id|nombre|especialidad";
        }

        if (!rol.equals("ADMINISTRADOR")) {
            return "ERROR|Solo administradores pueden agregar médicos";
        }

        try {
            Medico medico = new Medico();
            medico.setId(partes[1]);
            medico.setNombre(partes[2]);
            medico.setEspecialidad(partes[3]);

            // ✅ Crear administrador real con el usuario autenticado
            Administrador admin = obtenerAdministradorAutenticado();
            if (admin == null) {
                return "ERROR|No se pudo validar el administrador";
            }

            MedicoLogica medicoLogica = new MedicoLogica();
            medicoLogica.agregar(admin, medico);

            return "OK|Médico agregado exitosamente";

        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }

    private String procesarModificarMedico(String[] partes) {
        if (partes.length < 4) {
            return "ERROR|Formato: MODIFICAR_MEDICO|id|nombre|especialidad";
        }

        if (!rol.equals("ADMINISTRADOR")) {
            return "ERROR|Solo administradores pueden modificar médicos";
        }

        try {
            MedicoLogica medicoLogica = new MedicoLogica();

            // Buscar el médico existente para preservar la clave
            Medico medicoExistente = medicoLogica.buscarPorId(partes[1]);
            if (medicoExistente == null) {
                return "ERROR|Médico no encontrado";
            }

            // Actualizar datos
            medicoExistente.setNombre(partes[2]);
            medicoExistente.setEspecialidad(partes[3]);

            Administrador admin = obtenerAdministradorAutenticado();
            if (admin == null) {
                return "ERROR|No se pudo validar el administrador";
            }

            medicoLogica.modificar(admin, medicoExistente);

            return "OK|Médico modificado exitosamente";

        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }

    private String procesarEliminarMedico(String[] partes) {
        if (partes.length < 2) {
            return "ERROR|Formato: ELIMINAR_MEDICO|id";
        }

        if (!rol.equals("ADMINISTRADOR")) {
            return "ERROR|Solo administradores pueden eliminar médicos";
        }

        try {
            Administrador admin = obtenerAdministradorAutenticado();
            if (admin == null) {
                return "ERROR|No se pudo validar el administrador";
            }

            MedicoLogica medicoLogica = new MedicoLogica();
            medicoLogica.borrar(admin, partes[1]);

            return "OK|Médico eliminado exitosamente";

        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }

    // ==================== PACIENTES ====================

    private String procesarListarPacientes() {
        try {
            PacienteLogica pacienteLogica = new PacienteLogica();
            var pacientes = pacienteLogica.listar();

            StringBuilder sb = new StringBuilder("OK");
            for (Paciente p : pacientes) {
                sb.append("|").append(p.getId())
                        .append(",").append(p.getNombre())
                        .append(",").append(p.getTelefono())
                        .append(",").append(p.getFechaNacimiento());
            }

            return sb.toString();

        } catch (SQLException e) {
            return "ERROR|" + e.getMessage();
        }
    }

    private String procesarBuscarPaciente(String[] partes) {
        if (partes.length < 2) {
            return "ERROR|Formato: BUSCAR_PACIENTE|id";
        }

        try {
            PacienteLogica pacienteLogica = new PacienteLogica();
            Paciente p = pacienteLogica.buscarPorId(partes[1]);

            if (p == null) {
                return "ERROR|Paciente no encontrado";
            }

            return "OK|" + p.getId() + "," + p.getNombre() + ","
                    + p.getTelefono() + "," + p.getFechaNacimiento();

        } catch (SQLException e) {
            return "ERROR|" + e.getMessage();
        }
    }

    private String procesarAgregarPaciente(String[] partes) {
        if (partes.length < 5) {
            return "ERROR|Formato: AGREGAR_PACIENTE|id|nombre|telefono|fechaNacimiento(YYYY-MM-DD)";
        }

        try {
            Paciente paciente = new Paciente();
            paciente.setId(partes[1]);
            paciente.setNombre(partes[2]);
            paciente.setTelefono(partes[3]);
            paciente.setFechaNacimiento(java.time.LocalDate.parse(partes[4]));

            Administrador admin = obtenerAdministradorAutenticado();
            if (admin == null) {
                return "ERROR|No se pudo validar el administrador";
            }

            PacienteLogica pacienteLogica = new PacienteLogica();
            pacienteLogica.agregar(admin, paciente);

            return "OK|Paciente agregado exitosamente";

        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }

    // ==================== FARMACEUTAS ====================

    private String procesarListarFarmaceutas() {
        try {
            FarmaceutaLogica farmaceutaLogica = new FarmaceutaLogica();
            var farmaceutas = farmaceutaLogica.listar();

            StringBuilder sb = new StringBuilder("OK");
            for (Farmaceuta f : farmaceutas) {
                sb.append("|").append(f.getId())
                        .append(",").append(f.getNombre());
            }

            return sb.toString();

        } catch (SQLException e) {
            return "ERROR|" + e.getMessage();
        }
    }

    // ==================== MEDICAMENTOS ====================

    private String procesarListarMedicamentos() {
        try {
            MedicamentoLogica medicamentoLogica = new MedicamentoLogica();
            var medicamentos = medicamentoLogica.listar();

            StringBuilder sb = new StringBuilder("OK");
            for (Medicamento m : medicamentos) {
                sb.append("|").append(m.getCodigo())
                        .append(",").append(m.getNombre())
                        .append(",").append(m.getPresentacion());
            }

            return sb.toString();

        } catch (SQLException e) {
            return "ERROR|" + e.getMessage();
        }
    }

    // ==================== RECETAS ====================

    private String procesarListarRecetas() {
        try {
            RecetaLogica recetaLogica = new RecetaLogica();
            var recetas = recetaLogica.listar();

            StringBuilder sb = new StringBuilder("OK");
            for (Receta r : recetas) {
                sb.append("|").append(r.getId())
                        .append(",").append(r.getPaciente().getNombre())
                        .append(",").append(r.getMedico().getNombre())
                        .append(",").append(r.getFecha())
                        .append(",").append(r.getEstado());
            }

            return sb.toString();

        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }

    private String procesarActualizarEstadoReceta(String[] partes) {
        if (partes.length < 3) {
            return "ERROR|Formato: ACTUALIZAR_ESTADO_RECETA|recetaId|nuevoEstado";
        }

        if (!rol.equals("FARMACEUTA")) {
            return "ERROR|Solo farmaceutas pueden actualizar estados de recetas";
        }

        try {
            // Crear farmaceuta autenticado
            Farmaceuta farmaceuta = obtenerFarmaceutaAutenticado();
            if (farmaceuta == null) {
                return "ERROR|No se pudo validar el farmaceuta";
            }

            RecetaLogica recetaLogica = new RecetaLogica();
            EstadoReceta nuevoEstado = EstadoReceta.valueOf(partes[2]);
            recetaLogica.actualizarEstado(farmaceuta, partes[1], nuevoEstado);

            return "OK|Estado actualizado exitosamente";

        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }

    // ==================== CHAT ====================

    private String procesarMensajeChat(String[] partes) {
        if (partes.length < 3) {
            return "ERROR|Formato: MENSAJE|usuarioDestino|texto";
        }

        if (usuarioId == null) {
            return "ERROR|Debe estar autenticado";
        }

        String destinatario = partes[1];
        String texto = partes[2];

        String mensajeChat = "CHAT|" + nombre + "|" + destinatario + "|" + texto;
        server.broadcast(mensajeChat, this);

        return "OK|Mensaje enviado";
    }

    // ==================== UTILIDADES ====================

    private Administrador obtenerAdministradorAutenticado() {
        if (usuarioId == null || !rol.equals("ADMINISTRADOR")) {
            return null;
        }

        try {
            AdministradorLogica adminLogica = new AdministradorLogica();
            return adminLogica.buscarPorId(usuarioId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error obteniendo administrador", e);
            return null;
        }
    }

    private Farmaceuta obtenerFarmaceutaAutenticado() {
        if (usuarioId == null || !rol.equals("FARMACEUTA")) {
            return null;
        }

        try {
            FarmaceutaLogica farmaceutaLogica = new FarmaceutaLogica();
            return farmaceutaLogica.buscarPorId(usuarioId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error obteniendo farmaceuta", e);
            return null;
        }
    }

    private void cerrarConexion() {
        try {
            if (usuarioId != null) {
                server.broadcast("NOTIFICACION|LOGOUT|" + nombre + "|" + rol, this);
            }

            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();

            server.remove(this);

            LOGGER.info("Conexión cerrada para: " + nombre);

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error cerrando conexión: " + e.getMessage());
        }
    }

    public String getUsuarioId() { return usuarioId; }
    public String getNombreUsuario() { return nombre; }
    public String getRol() { return rol; }
}