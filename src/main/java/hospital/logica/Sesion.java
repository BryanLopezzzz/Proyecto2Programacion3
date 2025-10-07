package hospital.logica;

import hospital.model.Usuario;

public class Sesion {
    private static Usuario usuarioActual;

    public static void setUsuario(Usuario u) {
        usuarioActual = u;
    }

    public static Usuario getUsuario() {
        return usuarioActual;
    }
}
