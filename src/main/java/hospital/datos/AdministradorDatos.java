package hospital.datos;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import hospital.datos.conector.AdministradorConector;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import hospital.datos.entidades.AdministradorEntidad;

public class AdministradorDatos {
    private final Path path;
    private final JAXBContext context;
    private AdministradorConector cache;

    public AdministradorDatos(String filePath) {
        try {
            this.path = Path.of(Objects.requireNonNull(filePath));
            this.context = JAXBContext.newInstance(AdministradorConector.class, AdministradorEntidad.class);
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando AdministradorDatos: " + e.getMessage(), e);
        }
    }

    public synchronized AdministradorConector load() {
        try {
            if (cache != null) return cache;

            if (Files.notExists(path)) {
                cache = new AdministradorConector();
                save(cache);
                return cache;
            }

            Unmarshaller u = context.createUnmarshaller();
            cache = (AdministradorConector) u.unmarshal(path.toFile());

            if (cache.getAdministradores() == null) {
                cache.setAdministradores(new ArrayList<>());
            }
            return cache;
        } catch (Exception e) {
            throw new RuntimeException("Error cargando administradores: " + e.getMessage(), e);
        }
    }

    public synchronized void save(AdministradorConector data) throws JAXBException {
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
