package hospital.servicios;

import hospital.logica.*;
import hospital.model.*;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
    private boolean activo = true;

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
            String[] partes = mensaje.split("\\|",-1);
            String comando = partes[0];

            switch (comando) {
                // ===== AUTENTICACIÓN =====
                case "LOGIN":
                    return procesarLogin(partes);
                case "LOGOUT":
                    return procesarLogout();
                case "CAMBIAR_CLAVE":
                    return procesarCambiarClave(partes);

                // ===== MÉDICOS =====
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

                // ===== FARMACEUTAS =====
                case "LISTAR_FARMACEUTAS":
                    return procesarListarFarmaceutas();
                case "BUSCAR_FARMACEUTA":
                    return procesarBuscarFarmaceuta(partes);
                case "AGREGAR_FARMACEUTA":
                    return procesarAgregarFarmaceuta(partes);
                case "MODIFICAR_FARMACEUTA":
                    return procesarModificarFarmaceuta(partes);
                case "ELIMINAR_FARMACEUTA":
                    return procesarEliminarFarmaceuta(partes);

                // ===== PACIENTES =====
                case "LISTAR_PACIENTES":
                    return procesarListarPacientes();
                case "BUSCAR_PACIENTE":
                    return procesarBuscarPaciente(partes);
                case "AGREGAR_PACIENTE":
                    return procesarAgregarPaciente(partes);
                case "MODIFICAR_PACIENTE":
                    return procesarModificarPaciente(partes);
                case "ELIMINAR_PACIENTE":
                    return procesarEliminarPaciente(partes);

                // ===== MEDICAMENTOS =====
                case "LISTAR_MEDICAMENTOS":
                    return procesarListarMedicamentos();
                case "BUSCAR_MEDICAMENTO":
                    return procesarBuscarMedicamento(partes);
                case "AGREGAR_MEDICAMENTO":
                    return procesarAgregarMedicamento(partes);
                case "MODIFICAR_MEDICAMENTO":
                    return procesarModificarMedicamento(partes);
                case "ELIMINAR_MEDICAMENTO":
                    return procesarEliminarMedicamento(partes);

                // ===== RECETAS =====
                case "LISTAR_RECETAS":
                    return procesarListarRecetas();
                case "BUSCAR_RECETA":
                    return procesarBuscarReceta(partes);
                case "CREAR_RECETA":
                    return procesarCrearReceta(partes);
                case "ACTUALIZAR_ESTADO_RECETA":
                    return procesarActualizarEstadoReceta(partes);
                case "LISTAR_RECETAS_PACIENTE":
                    return procesarListarRecetasPaciente(partes);

                // ===== DASHBOARD =====
                case "DASHBOARD_ESTADISTICAS":
                    return procesarDashboardEstadisticas();

                // ===== MENSAJERÍA =====
                case "ENVIAR_MENSAJE":
                    return procesarEnviarMensaje(partes);
                case "LISTAR_USUARIOS_ACTIVOS":
                    return procesarListarUsuariosActivos();
                case "CARGAR_HISTORIAL":
                    return procesarCargarHistorial(partes);

                // ===== UTILIDAD =====
                case "PING":
                    return "PONG|Servidor activo";

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

            server.notificarLogin(this);

            return "OK|" + nombre + "|" + rol;

        } catch (Exception e) {
            LOGGER.warning("Login fallido para: " + id);
            return "ERROR|Credenciales incorrectas";
        }
    }

    private String procesarLogout() {
        if (usuarioId == null) {
            return "ERROR|No hay sesión activa";
        }

        server.notificarLogout(this);

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

        } catch (SQLException e) {
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
            Medico m = medicoLogica.buscarPorId(partes[1]);

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

            Medico medicoExistente = medicoLogica.buscarPorId(partes[1]);
            if (medicoExistente == null) {
                return "ERROR|Médico no encontrado";
            }

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

    private String procesarModificarPaciente(String[] partes) {
        if (partes.length < 5) {
            return "ERROR|Formato: MODIFICAR_PACIENTE|id|nombre|telefono|fechaNacimiento";
        }

        if (!rol.equals("ADMINISTRADOR")) {
            return "ERROR|Solo administradores pueden modificar pacientes";
        }

        try {
            PacienteLogica pacienteLogica = new PacienteLogica();

            Paciente pacienteExistente = pacienteLogica.buscarPorId(partes[1]);
            if (pacienteExistente == null) {
                return "ERROR|Paciente no encontrado";
            }

            pacienteExistente.setNombre(partes[2]);
            pacienteExistente.setTelefono(partes[3]);
            pacienteExistente.setFechaNacimiento(java.time.LocalDate.parse(partes[4]));

            Administrador admin = obtenerAdministradorAutenticado();
            if (admin == null) {
                return "ERROR|No se pudo validar el administrador";
            }

            pacienteLogica.modificar(admin, pacienteExistente);

            return "OK|Paciente modificado exitosamente";

        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }

    private String procesarEliminarPaciente(String[] partes) {
        if (partes.length < 2) {
            return "ERROR|Formato: ELIMINAR_PACIENTE|id";
        }

        if (!rol.equals("ADMINISTRADOR")) {
            return "ERROR|Solo administradores pueden eliminar pacientes";
        }

        try {
            Administrador admin = obtenerAdministradorAutenticado();
            if (admin == null) {
                return "ERROR|No se pudo validar el administrador";
            }

            PacienteLogica pacienteLogica = new PacienteLogica();
            pacienteLogica.eliminar(admin, partes[1]);

            return "OK|Paciente eliminado exitosamente";

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

    private String procesarBuscarFarmaceuta(String[] partes) {
        if (partes.length < 2) {
            return "ERROR|Formato: BUSCAR_FARMACEUTA|id";
        }

        try {
            FarmaceutaLogica farmaceutaLogica = new FarmaceutaLogica();
            Farmaceuta f = farmaceutaLogica.buscarPorId(partes[1]);

            if (f == null) {
                return "ERROR|Farmaceuta no encontrado";
            }

            return "OK|" + f.getId() + "," + f.getNombre();

        } catch (SQLException e) {
            return "ERROR|" + e.getMessage();
        }
    }

    private String procesarAgregarFarmaceuta(String[] partes) {
        if (partes.length < 3) {
            return "ERROR|Formato: AGREGAR_FARMACEUTA|id|nombre";
        }

        if (!rol.equals("ADMINISTRADOR")) {
            return "ERROR|Solo administradores pueden agregar farmaceutas";
        }

        try {
            Farmaceuta farmaceuta = new Farmaceuta();
            farmaceuta.setId(partes[1]);
            farmaceuta.setNombre(partes[2]);

            Administrador admin = obtenerAdministradorAutenticado();
            if (admin == null) {
                return "ERROR|No se pudo validar el administrador";
            }

            FarmaceutaLogica farmaceutaLogica = new FarmaceutaLogica();
            farmaceutaLogica.agregar(admin, farmaceuta);

            return "OK|Farmaceuta agregado exitosamente";

        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }

    private String procesarModificarFarmaceuta(String[] partes) {
        if (partes.length < 3) {
            return "ERROR|Formato: MODIFICAR_FARMACEUTA|id|nombre";
        }

        if (!rol.equals("ADMINISTRADOR")) {
            return "ERROR|Solo administradores pueden modificar farmaceutas";
        }

        try {
            FarmaceutaLogica farmaceutaLogica = new FarmaceutaLogica();

            Farmaceuta farmaceutaExistente = farmaceutaLogica.buscarPorId(partes[1]);
            if (farmaceutaExistente == null) {
                return "ERROR|Farmaceuta no encontrado";
            }

            farmaceutaExistente.setNombre(partes[2]);

            Administrador admin = obtenerAdministradorAutenticado();
            if (admin == null) {
                return "ERROR|No se pudo validar el administrador";
            }

            farmaceutaLogica.modificar(admin, farmaceutaExistente);

            return "OK|Farmaceuta modificado exitosamente";

        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }

    private String procesarEliminarFarmaceuta(String[] partes) {
        if (partes.length < 2) {
            return "ERROR|Formato: ELIMINAR_FARMACEUTA|id";
        }

        if (!rol.equals("ADMINISTRADOR")) {
            return "ERROR|Solo administradores pueden eliminar farmaceutas";
        }

        try {
            Administrador admin = obtenerAdministradorAutenticado();
            if (admin == null) {
                return "ERROR|No se pudo validar el administrador";
            }

            FarmaceutaLogica farmaceutaLogica = new FarmaceutaLogica();
            farmaceutaLogica.borrar(admin, partes[1]);

            return "OK|Farmaceuta eliminado exitosamente";

        } catch (Exception e) {
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

private String procesarBuscarMedicamento(String[] partes) {
    if (partes.length < 2) {
        return "ERROR|Formato: BUSCAR_MEDICAMENTO|codigo";
    }

    try {
        MedicamentoLogica medicamentoLogica = new MedicamentoLogica();
        Medicamento m = medicamentoLogica.buscarPorCodigo(partes[1]);

        if (m == null) {
            return "ERROR|Medicamento no encontrado";
        }

        return "OK|" + m.getCodigo() + "," + m.getNombre() + "," + m.getPresentacion();

    } catch (SQLException e) {
        return "ERROR|" + e.getMessage();
    }
}

private String procesarAgregarMedicamento(String[] partes) {
    if (partes.length < 4) {
        return "ERROR|Formato: AGREGAR_MEDICAMENTO|codigo|nombre|presentacion";
    }

    if (!rol.equals("ADMINISTRADOR")) {
        return "ERROR|Solo administradores pueden agregar medicamentos";
    }

    try {
        Medicamento medicamento = new Medicamento();
        medicamento.setCodigo(partes[1]);
        medicamento.setNombre(partes[2]);
        medicamento.setPresentacion(partes[3]);

        Administrador admin = obtenerAdministradorAutenticado();
        if (admin == null) {
            return "ERROR|No se pudo validar el administrador";
        }

        MedicamentoLogica medicamentoLogica = new MedicamentoLogica();
        medicamentoLogica.agregar(admin, medicamento);

        return "OK|Medicamento agregado exitosamente";

    } catch (Exception e) {
        return "ERROR|" + e.getMessage();
    }
}

private String procesarModificarMedicamento(String[] partes) {
    if (partes.length < 4) {
        return "ERROR|Formato: MODIFICAR_MEDICAMENTO|codigo|nombre|presentacion";
    }

    if (!rol.equals("ADMINISTRADOR")) {
        return "ERROR|Solo administradores pueden modificar medicamentos";
    }

    try {
        MedicamentoLogica medicamentoLogica = new MedicamentoLogica();

        Medicamento medicamentoExistente = medicamentoLogica.buscarPorCodigo(partes[1]);
        if (medicamentoExistente == null) {
            return "ERROR|Medicamento no encontrado";
        }

        medicamentoExistente.setNombre(partes[2]);
        medicamentoExistente.setPresentacion(partes[3]);

        Administrador admin = obtenerAdministradorAutenticado();
        if (admin == null) {
            return "ERROR|No se pudo validar el administrador";
        }

        medicamentoLogica.modificar(admin, medicamentoExistente);

        return "OK|Medicamento modificado exitosamente";

    } catch (Exception e) {
        return "ERROR|" + e.getMessage();
    }
}

private String procesarEliminarMedicamento(String[] partes) {
    if (partes.length < 2) {
        return "ERROR|Formato: ELIMINAR_MEDICAMENTO|codigo";
    }

    if (!rol.equals("ADMINISTRADOR")) {
        return "ERROR|Solo administradores pueden eliminar medicamentos";
    }

    try {
        Administrador admin = obtenerAdministradorAutenticado();
        if (admin == null) {
            return "ERROR|No se pudo validar el administrador";
        }

        MedicamentoLogica medicamentoLogica = new MedicamentoLogica();
        medicamentoLogica.borrar(admin, partes[1]);

        return "OK|Medicamento eliminado exitosamente";

    } catch (Exception e) {
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

    private String procesarBuscarReceta(String[] partes) {
        if (partes.length < 2) {
            return "ERROR|Formato: BUSCAR_RECETA|id";
        }

        try {
            RecetaLogica recetaLogica = new RecetaLogica();
            Receta r = recetaLogica.buscarPorId(partes[1]);

            if (r == null) {
                return "ERROR|Receta no encontrada";
            }

            return "OK|" + r.getId() + "," + r.getPaciente().getNombre() + ","
                    + r.getMedico().getNombre() + "," + r.getFecha() + "," + r.getEstado();

        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }

    private String procesarCrearReceta(String[] partes) {

        if (partes.length < 8) {
            return "ERROR|Formato incorrecto. Mínimo: CREAR_RECETA|recetaId|pacienteId|medicoId|fecha|fechaRetiro|estado|numDetalles";
        }

        if (!rol.equals("MEDICO")) {
            return "ERROR|Solo los médicos pueden crear recetas";
        }

        try {
            String recetaId = partes[1];
            String pacienteId = partes[2];
            String medicoId = partes[3];
            String fechaStr = partes[4];
            String fechaRetiroStr = partes[5];
            String estadoStr = partes[6];
            int numDetalles = Integer.parseInt(partes[7]);

            if (!medicoId.equals(usuarioId)) {
                return "ERROR|Solo puede crear recetas como el médico autenticado";
            }

            if (partes.length < 8 + numDetalles) {
                return "ERROR|Faltan detalles de medicamentos. Se esperaban " + numDetalles;
            }

            PacienteLogica pacienteLogica = new PacienteLogica();
            Paciente paciente = pacienteLogica.buscarPorId(pacienteId);
            if (paciente == null) {
                return "ERROR|Paciente no encontrado: " + pacienteId;
            }

            MedicoLogica medicoLogica = new MedicoLogica();
            Medico medico = medicoLogica.buscarPorId(medicoId);
            if (medico == null) {
                return "ERROR|Médico no encontrado: " + medicoId;
            }

            Receta receta = new Receta();
            receta.setId(recetaId);
            receta.setPaciente(paciente);
            receta.setMedico(medico);
            receta.setFecha(java.time.LocalDate.parse(fechaStr));
            receta.setFechaRetiro(java.time.LocalDate.parse(fechaRetiroStr));
            receta.setEstado(EstadoReceta.valueOf(estadoStr));

            List<DetalleReceta> detalles = new ArrayList<>();
            MedicamentoLogica medicamentoLogica = new MedicamentoLogica();

            for (int i = 0; i < numDetalles; i++) {
                String detalleStr = partes[8 + i];
                String[] detalleParts = detalleStr.split(",", 4);

                if (detalleParts.length < 4) {
                    return "ERROR|Formato de detalle inválido en posición " + i +
                            ". Esperado: medCodigo,cantidad,indicaciones,dias";
                }

                String medCodigo = detalleParts[0];
                int cantidad = Integer.parseInt(detalleParts[1]);
                String indicaciones = detalleParts[2];
                int dias = Integer.parseInt(detalleParts[3]);

                Medicamento medicamento = medicamentoLogica.buscarPorCodigo(medCodigo);
                if (medicamento == null) {
                    return "ERROR|Medicamento no encontrado: " + medCodigo;
                }

                DetalleReceta detalle = new DetalleReceta();
                detalle.setMedicamento(medicamento);
                detalle.setCantidad(cantidad);
                detalle.setIndicaciones(indicaciones);
                detalle.setDiasTratamiento(dias);

                detalles.add(detalle);
            }

            receta.setDetalles(detalles);

            RecetaLogica recetaLogica = new RecetaLogica();
            Receta recetaCreada = recetaLogica.crearReceta(receta);

            LOGGER.info("Receta creada: " + recetaId + " por médico " + medicoId);

            return "OK|Receta creada exitosamente|" + recetaCreada.getId();

        } catch (NumberFormatException e) {
            return "ERROR|Formato numérico inválido: " + e.getMessage();
        } catch (java.time.format.DateTimeParseException e) {
            return "ERROR|Formato de fecha inválido. Use YYYY-MM-DD";
        } catch (IllegalArgumentException e) {
            return "ERROR|Estado de receta inválido: " + e.getMessage();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creando receta", e);
            return "ERROR|Error al crear receta: " + e.getMessage();
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

    private String procesarListarRecetasPaciente(String[] partes) {
        if (partes.length < 2) {
            return "ERROR|Formato: LISTAR_RECETAS_PACIENTE|pacienteId";
        }

        try {
            RecetaLogica recetaLogica = new RecetaLogica();
            var recetas = recetaLogica.listarRecetasPorPaciente(partes[1]);

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

// ==================== DASHBOARD ====================

    private String procesarDashboardEstadisticas() {
        try {
            EstadisticaRecetaLogica estadisticaLogica = new EstadisticaRecetaLogica();

            // Obtener estadísticas por estado
            var recetasPorEstado = estadisticaLogica.recetasPorEstado();

            StringBuilder sb = new StringBuilder("OK");
            for (var entry : recetasPorEstado.entrySet()) {
                sb.append("|").append(entry.getKey()).append(",").append(entry.getValue());
            }

            return sb.toString();

        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }

    // ==================== CHAT ====================

    private String procesarEnviarMensaje(String[] partes) {
        if (partes.length < 3) {
            return "ERROR|Formato: ENVIAR_MENSAJE|usuarioDestinoId|mensaje";
        }

        if (usuarioId == null) {
            return "ERROR|Debe estar autenticado para enviar mensajes";
        }

        String destinatarioId = partes[1];
        String mensaje = partes[2];

        boolean enviado = server.enviarMensajePrivado(this, destinatarioId, mensaje);

        if (enviado) {
            return "OK|Mensaje enviado a " + destinatarioId;
        } else {
            return "ERROR|Usuario destinatario no está conectado o no existe";
        }
    }

    private String procesarListarUsuariosActivos() {
        if (usuarioId == null) {
            return "ERROR|Debe estar autenticado";
        }

        var usuariosActivos = server.obtenerUsuariosActivos();

        StringBuilder sb = new StringBuilder("OK");
        for (ClientHandler cliente : usuariosActivos) {
            // No incluir al usuario actual en la lista
            if (cliente != this && cliente.isAutenticado()) {
                sb.append("|")
                        .append(cliente.getUsuarioId())
                        .append(",")
                        .append(cliente.getNombreUsuario())
                        .append(",")
                        .append(cliente.getRol());
            }
        }

        return sb.toString();
    }

    private String procesarCargarHistorial(String[] partes) {
        if (partes.length < 2) {
            return "ERROR|Formato: CARGAR_HISTORIAL|otroUsuarioId";
        }

        if (usuarioId == null) {
            return "ERROR|Debe estar autenticado";
        }

        String otroUsuario = partes[1];
        List<HospitalServer.MensajeHistorial> historial =
                server.obtenerHistorial(usuarioId, otroUsuario);

        if (historial.isEmpty()) {
            return "OK";
        }

        StringBuilder sb = new StringBuilder("OK");
        for (var msg : historial) {
            sb.append("|")
                    .append(msg.getRemitenteId()).append(",")
                    .append(msg.getMensaje()).append(",")
                    .append(msg.getFecha());
        }

        return sb.toString();
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

    public boolean isAutenticado() {
        return usuarioId != null && !usuarioId.equals("anonimo");
    }

    public boolean isActivo() {
        return activo;
    }

    public String getUsuarioId() { return usuarioId; }
    public String getNombreUsuario() { return nombre; }
    public String getRol() { return rol; }
}