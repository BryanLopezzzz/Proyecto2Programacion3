package hospital.logica;

import hospital.model.EstadoReceta;
import hospital.model.Receta;
import hospital.model.Usuario;

import java.util.List;
import java.util.stream.Collectors;

public class HistoricoRecetasLogica {
    private final RecetaLogica recetaLogica;

    public HistoricoRecetasLogica() {
        this.recetaLogica = new RecetaLogica();
    }

    // Lista todas las recetas (sin modificar nada)
    public List<Receta> listarRecetas(Usuario usuario) {
        // en este caso cualquier usuario puede ver el histórico
        return recetaLogica.listar();
    }

    // Buscar receta por ID
    public Receta buscarPorId(Usuario usuario, String recetaId) {
        return recetaLogica.buscarPorId(recetaId);
    }

    // Buscar recetas por paciente (filtrando sobre la lista general)
    public List<Receta> buscarPorPaciente(Usuario usuario, String criterio) {
        return recetaLogica.listar().stream()
                .filter(r -> r.getPaciente() != null
                        && (r.getPaciente().getId().equalsIgnoreCase(criterio)
                        || r.getPaciente().getNombre().toLowerCase().contains(criterio.toLowerCase())))
                .collect(Collectors.toList());
    }

    // Buscar recetas por médico
    public List<Receta> buscarPorMedico(Usuario usuario, String criterio) {
        return recetaLogica.listar().stream()
                .filter(r -> r.getMedico() != null
                        && (r.getMedico().getId().equalsIgnoreCase(criterio)
                        || r.getMedico().getNombre().toLowerCase().contains(criterio.toLowerCase())))
                .collect(Collectors.toList());
    }

    // Buscar recetas por estado
    public List<Receta> buscarPorEstado(Usuario usuario, EstadoReceta estado) {
        return recetaLogica.listar().stream()
                .filter(r -> r.getEstado() == estado)
                .collect(Collectors.toList());
    }
}
