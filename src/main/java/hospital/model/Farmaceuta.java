package hospital.model;

public class Farmaceuta extends Usuario {

    public Farmaceuta() {
    }

    public Farmaceuta(String id, String clave, String nombre) {
        super(id, clave, nombre);
    }

    @Override
    public String toString() {
        return getNombre();
    }
}