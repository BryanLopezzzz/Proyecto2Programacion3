package hospital.datos;

import jakarta.xml.bind.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import hospital.datos.conector.PacienteConector;
import hospital.datos.entidades.PacienteEntidad;

public class PacienteDatos {
    private final Path path;
    private final JAXBContext context;
    private PacienteConector cache;

    public PacienteDatos(String filePath) {
        try {
            this.path = Path.of(Objects.requireNonNull(filePath));
            this.context = JAXBContext.newInstance(PacienteConector.class, PacienteEntidad.class);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public synchronized PacienteConector load() {
        try {
            if (cache != null) return cache;

            if (Files.notExists(path)) {
                cache = new PacienteConector();
                save(cache);
                return cache;
            }

            Unmarshaller u = context.createUnmarshaller();
            cache = (PacienteConector) u.unmarshal(path.toFile());

            if (cache.getPacientes() == null) {
                cache.setPacientes(new java.util.ArrayList<>());
            }
            return cache;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public synchronized void save(PacienteConector data) throws JAXBException {
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

        File out = path.toFile();
        File parent = out.getParentFile();

        if(parent != null){
            parent.mkdirs();
        }

        java.io.StringWriter sw = new java.io.StringWriter();
        m.marshal(data, sw);
        m.marshal(data,out);
    }

    public Path getPath() { return path; }
}