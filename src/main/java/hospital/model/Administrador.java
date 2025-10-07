package hospital.model;

public class Administrador extends Usuario {
    public Administrador() {
    }

    public Administrador(String id, String clave, String nombre) {
        super(id, clave, nombre);
    }

    @Override
    public String toString() {
        return "Administrador: " + getNombre();
    }
}
