package hospital.datos;

import hospital.model.Medicamento;
import hospital.logica.mapper.MedicamentoMapper;
import hospital.datos.conector.MedicamentoConector;
import jakarta.xml.bind.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class MedicamentoDatos {

    private final Path path;
    private final JAXBContext context;
    private MedicamentoConector cache;

    public MedicamentoDatos(String filePath) {
        try {
            this.path = Path.of(Objects.requireNonNull(filePath));
            this.context = JAXBContext.newInstance(MedicamentoConector.class, Medicamento.class);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public synchronized MedicamentoConector load() {
        try {
            if (cache != null) return cache;

            if (Files.notExists(path)) {
                cache = new MedicamentoConector();
                save(cache);
                return cache;
            }

            Unmarshaller u = context.createUnmarshaller();
            cache = (MedicamentoConector) u.unmarshal(path.toFile());

            if (cache.getMedicamentos() == null) {
                cache.setMedicamentos(new java.util.ArrayList<>());
            }
            return cache;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public synchronized void save(MedicamentoConector data) throws JAXBException {
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

    public Path getPath() {
        return path;
    }

    // Búsqueda rápida por código
    public Medicamento buscarPorCodigo(String codigo) {
        return load().getMedicamentos().stream()
                .map(MedicamentoMapper::fromXML)   // convertir a Medicamento
                .filter(m -> m.getCodigo().equalsIgnoreCase(codigo))
                .findFirst()
                .orElse(null);                     // ahora devuelve un Medicamento
    }
}