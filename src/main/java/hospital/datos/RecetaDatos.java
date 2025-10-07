package hospital.datos;

import jakarta.xml.bind.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import hospital.datos.entidades.RecetaEntidad;
import hospital.datos.conector.RecetaConector;

public class RecetaDatos {
    private final Path path;
    private final JAXBContext context;
    private RecetaConector cache;

    public RecetaDatos(String filePath) {
        try {
            this.path = Path.of(Objects.requireNonNull(filePath));
            this.context = JAXBContext.newInstance(RecetaConector.class, RecetaEntidad.class);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public synchronized RecetaConector load() {
        try {
            if (Files.notExists(path)) {
                RecetaConector nuevo = new RecetaConector();
                save(nuevo);
                return nuevo;
            }
            Unmarshaller u = context.createUnmarshaller();
            RecetaConector data = (RecetaConector) u.unmarshal(path.toFile());
            if (data.getRecetas() == null) data.setRecetas(new ArrayList<>());
            return data;
        } catch (Exception e) {
            throw new RuntimeException("Error cargando recetas: " + e.getMessage(), e);
        }
    }

    public synchronized void save(RecetaConector data) throws JAXBException {
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
