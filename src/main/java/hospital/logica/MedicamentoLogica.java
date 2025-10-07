package hospital.logica;

import hospital.datos.MedicamentoDatos;
import hospital.datos.conector.MedicamentoConector;
import hospital.datos.conector.MedicoConector;
import hospital.datos.entidades.MedicamentoEntidad;
import hospital.datos.entidades.MedicoEntidad;
import hospital.logica.mapper.MedicamentoMapper;
import hospital.model.Medicamento;
import hospital.model.Medico;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MedicamentoLogica {
    private final MedicamentoDatos datos;

    public MedicamentoLogica() {
        this.datos = new MedicamentoDatos("data/medicamentos.xml");
    }

    public void agregar(Medicamento medicamento) throws Exception {
        validarMedicamento(medicamento);

        MedicamentoConector conector = datos.load();
        conector.getMedicamentos().add(MedicamentoMapper.toXML(medicamento));
        datos.save(conector);
    }

    public List<Medicamento> listar() {
        return datos.load().getMedicamentos().stream()
                .map(MedicamentoMapper::fromXML)   // Convertir cada MedicamentoEntidad a Medicamento
                .collect(Collectors.toList());
    }

    public void modificar(Medicamento medicamento) throws Exception {
        validarMedicamento(medicamento);

        MedicamentoConector con = datos.load();
        MedicamentoEntidad actual = con.getMedicamentos().stream()
                .filter(m -> m.getCodigo().equalsIgnoreCase(medicamento.getCodigo()))
                .findFirst()
                .orElseThrow();

        actual.setNombre(medicamento.getNombre());
        actual.setPresentacion(medicamento.getPresentacion());

        ordenarPorNombre(con);
        datos.save(con);
    }

    private void validarMedicamento(Medicamento m) throws Exception {
        if (m == null) throw new Exception("El medicamento no puede ser nulo.");
        if (m.getCodigo() == null || m.getCodigo().isBlank()) throw new Exception("El código es obligatorio.");
        if (m.getNombre() == null || m.getNombre().isBlank()) throw new Exception("El nombre es obligatorio.");
        if (m.getPresentacion() == null || m.getPresentacion().isBlank()) throw new Exception("La presentación es obligatoria.");
    }

    private void ordenarPorNombre(MedicamentoConector con) {
        con.getMedicamentos().sort(Comparator.comparing(
                e -> Objects.toString(e.getNombre(), ""), String.CASE_INSENSITIVE_ORDER
        ));
    }

    //Clase

    public Medicamento actualizar(Medicamento actualizado) throws Exception {
        validarMedicamento(actualizado);

        MedicamentoConector data = datos.load();
        for (int i = 0; i < data.getMedicamentos().size(); i++) {
            var actual = data.getMedicamentos().get(i);

            if (actual.getCodigo().equalsIgnoreCase(actualizado.getCodigo())) {
                data.getMedicamentos().set(i, MedicamentoMapper.toXML(actualizado));
                datos.save(data);
                return actualizado;
            }
        }
        throw new Exception("No existe medicamento con código: " + actualizado.getCodigo());
    }

    public boolean eliminar(String codigo) throws Exception {
        MedicamentoConector conector = datos.load();
        boolean eliminado = conector.getMedicamentos().removeIf(r -> r.getCodigo().equalsIgnoreCase(codigo));
        if (eliminado) {
            datos.save(conector);
        }
        return eliminado;
    }

    public Medicamento buscarPorCodigo(String codigo) {
        return datos.load().getMedicamentos().stream()
                .filter(m -> m.getCodigo().equalsIgnoreCase(codigo))
                .map(MedicamentoMapper::fromXML)
                .findFirst()
                .orElse(null);
    }

    public List<Medicamento> buscarPorNombre(String nombre) {
        return datos.load().getMedicamentos().stream()
                .map(MedicamentoMapper::fromXML)   // Convertir cada MedicamentoEntidad a Medicamento
                .filter(m -> m.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void generarReporte(String rutaReporte) {
        try {
            // Obtener todos los medicamentos
            List<Medicamento> lista = listar();

            // Mapearlos a entidades XML
            MedicamentoConector conector = new MedicamentoConector();
            for (Medicamento m : lista) {
                conector.getMedicamentos().add(MedicamentoMapper.toXML(m));
            }

            JAXBContext context = JAXBContext.newInstance(MedicamentoConector.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            marshaller.marshal(conector, new File(rutaReporte));

        } catch (Exception e) {
            throw new RuntimeException("Error al generar reporte: " + e.getMessage(), e);
        }
    }

}
