package hospital.logica;

import hospital.datos.PacienteDatos;
import hospital.datos.conector.MedicamentoConector;
import hospital.datos.conector.PacienteConector;
import hospital.datos.conector.RecetaConector;
import hospital.datos.entidades.MedicamentoEntidad;
import hospital.datos.entidades.PacienteEntidad;
import hospital.logica.mapper.MedicamentoMapper;
import hospital.logica.mapper.PacienteMapper;
import hospital.logica.mapper.RecetaMapper;
import hospital.model.Medicamento;
import hospital.model.Paciente;
import hospital.model.Receta;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PacienteLogica {
    private final PacienteDatos datos;

    public PacienteLogica() {
        this.datos = new PacienteDatos("data/pacientes.xml");
    }

    public void agregar(Paciente paciente) throws Exception {
        validarPaciente(paciente);

        PacienteConector conector = datos.load();
        conector.getPacientes().add(PacienteMapper.toXML(paciente));
        datos.save(conector);
    }

    public List<Paciente> listar() {
        return datos.load().getPacientes().stream()
                .map(PacienteMapper::fromXML)
                .collect(Collectors.toList());
    }

    private void validarPaciente(Paciente p) throws Exception {
        if (p == null) throw new Exception("El paciente no puede ser nulo.");
        if (p.getId() == null || p.getId().isBlank()) throw new Exception("El ID es obligatorio.");
        if (p.getNombre() == null || p.getNombre().isBlank()) throw new Exception("El nombre es obligatorio.");
        if (p.getFechaNacimiento() == null || p.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new Exception("La fecha de nacimiento no es válida.");
        }
        if (p.getTelefono() == null || p.getTelefono().isBlank()) {
            throw new Exception("El teléfono es obligatorio.");
        }
    }
    //Clase
    public Paciente actualizar(Paciente actualizado) throws Exception {
        validarPaciente(actualizado);

        PacienteConector data = datos.load();
        for (int i = 0; i < data.getPacientes().size(); i++) {
            var actual = data.getPacientes().get(i);

            if (actual.getId().equalsIgnoreCase(actualizado.getId())) {
                data.getPacientes().set(i, PacienteMapper.toXML(actualizado));

                datos.save(data);
                return actualizado;
            }
        }
        throw new Exception("No existe paciente con id: " + actualizado.getId());
    }

    public boolean eliminar(String id) throws Exception {
        PacienteConector conector = datos.load();
        boolean eliminado = conector.getPacientes().removeIf(r -> r.getId().equalsIgnoreCase(id));
        if (eliminado) {
            datos.save(conector);
        }
        return eliminado;
    }
    public Paciente buscarPorId(String pacienteId) {
        return datos.load().getPacientes().stream()
                .map(PacienteMapper::fromXML) // usa el mapper sencillo
                .filter(p -> p.getId().equalsIgnoreCase(pacienteId))
                .findFirst()
                .orElse(null);
    }

    public void generarReporte(String rutaReporte) {
        try {
            // Obtener todos los pacietnes
            List<Paciente> lista = listar();

            // Mapearlos a entidades XML
            PacienteConector conector = new PacienteConector();
            for (Paciente m : lista) {
                conector.getPacientes().add(PacienteMapper.toXML(m));
            }

            JAXBContext context = JAXBContext.newInstance(PacienteConector.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            marshaller.marshal(conector, new File(rutaReporte));

        } catch (Exception e) {
            throw new RuntimeException("Error al generar reporte: " + e.getMessage(), e);
        }
    }


}