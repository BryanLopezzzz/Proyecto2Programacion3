package hospital.Intermediaria;

import hospital.model.Medicamento;
import hospital.model.Administrador;
import hospital.logica.MedicamentoLogica;

import java.util.List;

public class MedicamentoIntermediaria {

    private final MedicamentoLogica medicamentoLogica;

    public MedicamentoIntermediaria() {
        this.medicamentoLogica = new MedicamentoLogica();
    }

    public void agregar(Administrador admin, Medicamento medicamento) throws Exception {
        validarAdmin(admin);
        medicamentoLogica.agregar(medicamento);
    }

    public List<Medicamento> listar(Administrador admin) throws Exception {
        validarAdmin(admin);
        return medicamentoLogica.listar();
    }

    public Medicamento buscarPorCodigo(Administrador admin, String codigo) throws Exception {
        validarAdmin(admin);
        return medicamentoLogica.buscarPorCodigo(codigo);
    }

    public List<Medicamento> buscarPorNombre(Administrador admin, String nombre) throws Exception {
        validarAdmin(admin);
        return medicamentoLogica.buscarPorNombre(nombre);
    }

    private void validarAdmin(Administrador admin) throws Exception {
        if (admin == null) {
            throw new Exception("Solo los administradores pueden ejecutar esta acción.");
        }
    }

    public void modificar(Administrador admin, Medicamento medicamento) throws Exception {
        validarAdmin(admin);
        medicamentoLogica.modificar(medicamento);
    }

    public void borrar(Administrador admin, String codigo) throws Exception {
        validarAdmin(admin);
        medicamentoLogica.eliminar(codigo);
    }

    public void generarReporte(Administrador admin, String rutaReporte) throws Exception {
        validarAdmin(admin);
        medicamentoLogica.generarReporte(rutaReporte);
    }


}