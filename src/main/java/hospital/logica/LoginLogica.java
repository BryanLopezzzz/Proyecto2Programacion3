package hospital.logica;

import hospital.model.Usuario;

public class LoginLogica {
    private final UsuarioManager usuarioManager;

    public LoginLogica() {
        this.usuarioManager = new UsuarioManager();
    }

    // ==== LOGIN ====
    public Usuario login(String id, String clave) throws Exception {
        Usuario u = usuarioManager.login(id, clave);
        Sesion.setUsuario(u);
        return u;
    }


    // ==== LOGOUT ====
    public void logout() {
        Sesion.setUsuario(null);
    }

    // ==== CAMBIO DE CLAVE ====
    public void cambiarClave(String actual, String nueva) throws Exception {
        Usuario usuarioActual = Sesion.getUsuario();
        if (usuarioActual == null) {
            throw new Exception("No hay un usuario autenticado.");
        }

        if (!usuarioActual.getClave().equals(actual)) {
            throw new Exception("Clave actual incorrecta.");
        }
        usuarioActual.setClave(nueva);

        // actualizar segun el tipo de dato
        UsuarioManager.TipoUsuario tipo = new UsuarioManager().determinarTipoUsuario(usuarioActual.getId());
        switch (tipo) {
            case ADMINISTRADOR:
                new hospital.logica.AdministradorLogica().actualizar((hospital.model.Administrador) usuarioActual);
                break;
            case MEDICO:
                new hospital.logica.MedicoLogica().actualizar((hospital.model.Medico) usuarioActual);
                break;
            case FARMACEUTA:
                new hospital.logica.FarmaceutaLogica().actualizar((hospital.model.Farmaceuta) usuarioActual);
                break;
            default:
                throw new Exception("Tipo de usuario desconocido, no se puede guardar la clave.");
        }
        Sesion.setUsuario(usuarioActual);
    }

    // ==== Estado de sesión ====
    public Usuario getUsuarioActual() {
        return UsuarioManager.getUsuarioActual();
    }

    public boolean isLoggedIn() {
        return UsuarioManager.getUsuarioActual() != null;
    }
}
