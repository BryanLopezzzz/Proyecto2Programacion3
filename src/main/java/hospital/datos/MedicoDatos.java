package hospital.datos;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import hospital.datos.conector.MedicoConector;
import hospital.datos.entidades.MedicoEntidad;

public class MedicoDatos {
    private final Path path;
    private final JAXBContext context;
    private MedicoConector cache;

    public MedicoDatos(String filePath) {
        try {
            this.path = Path.of(Objects.requireNonNull(filePath));
            this.context = JAXBContext.newInstance(MedicoConector.class, MedicoEntidad.class);
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando MedicoDatos: " + e.getMessage(), e);
        }
    }

    public synchronized MedicoConector load() {
        try {
            if (cache != null) return cache;

            if (Files.notExists(path)) {
                cache = new MedicoConector();
                save(cache);
                return cache;
            }

            Unmarshaller u = context.createUnmarshaller();
            cache = (MedicoConector) u.unmarshal(path.toFile());

            if (cache.getMedicos() == null) {
                cache.setMedicos(new java.util.ArrayList<>());
            }
            return cache;
        } catch (Exception e) {
            throw new RuntimeException("Error cargando médicos: " + e.getMessage(), e);
        }
    }

    public synchronized void save(MedicoConector data) throws JAXBException {
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
