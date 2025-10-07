package hospital.Intermediaria;

import hospital.model.Administrador;
import hospital.model.Medico;
import hospital.logica.MedicoLogica;

import java.util.List;


public class MedicoIntermediaria {

    private final MedicoLogica logica;

    public MedicoIntermediaria() {
        this.logica = new MedicoLogica();
    }

    public void agregar(Administrador admin, Medico medico) throws Exception {
        validarAdmin(admin);
        logica.agregar(medico);
    }

    public List<Medico> listar(Administrador admin) throws Exception {
        validarAdmin(admin);
        return logica.listar();
    }

    public Medico buscarPorId(Administrador admin, String id) throws Exception {
        validarAdmin(admin);
        return logica.buscarPorId(id);
    }

    public List<Medico> buscarPorNombre(Administrador admin, String nombre) throws Exception {
        validarAdmin(admin);
        return logica.buscarPorNombre(nombre);
    }

    public void modificar(Administrador admin, Medico medico) throws Exception {
        validarAdmin(admin);
        logica.modificar(medico);
    }

    public void borrar(Administrador admin, String id) throws Exception {
        validarAdmin(admin);
        logica.eliminar(id);
    }

    private void validarAdmin(Administrador admin) throws Exception {
        if (admin == null) {
            throw new Exception("Solo los administradores pueden ejecutar esta acción.");
        }
    }

    public void generarReporte(Administrador admin, String rutaReporte) throws Exception {
        validarAdmin(admin);
        logica.generarReporte(rutaReporte);
    }
}