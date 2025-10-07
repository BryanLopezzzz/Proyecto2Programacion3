package hospital.datos;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import hospital.datos.conector.FarmaceutaConector;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import hospital.datos.entidades.FarmaceutaEntidad;

public class FarmaceutaDatos {
    private final Path path;
    private final JAXBContext context;
    private FarmaceutaConector cache;

    public FarmaceutaDatos(String filePath) {
        try {
            this.path = Path.of(Objects.requireNonNull(filePath));
            this.context = JAXBContext.newInstance(FarmaceutaConector.class, FarmaceutaEntidad.class);
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando FarmaceutaDatos: " + e.getMessage(), e);
        }
    }

    public synchronized FarmaceutaConector load() {
        try {
            if (cache != null) return cache;

            if (Files.notExists(path)) {
                cache = new FarmaceutaConector();
                save(cache);
                return cache;
            }

            Unmarshaller u = context.createUnmarshaller();
            cache = (FarmaceutaConector) u.unmarshal(path.toFile());

            if (cache.getFarmaceutas() == null) {
                cache.setFarmaceutas(new java.util.ArrayList<>());
            }
            return cache;
        } catch (Exception e) {
            throw new RuntimeException("Error cargando farmaceutas: " + e.getMessage(), e);
        }
    }

    public synchronized void save(FarmaceutaConector data) throws JAXBException {
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