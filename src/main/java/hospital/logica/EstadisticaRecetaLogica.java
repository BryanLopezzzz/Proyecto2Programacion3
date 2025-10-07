package hospital.logica;

import hospital.model.Receta;
import hospital.model.EstadoReceta;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class EstadisticaRecetaLogica {
    private final RecetaLogica recetaLogica;

    public EstadisticaRecetaLogica() {
        this.recetaLogica = new RecetaLogica();
    }

    public List<Receta> cargarRecetas() {
        return recetaLogica.listar();
    }

    public int totalRecetas() {
        return cargarRecetas().size();
    }

    // Estadística: recetas por estado (similar a clientesPorRangoEdad)
    public LinkedHashMap<String, Long> recetasPorEstado() {
        LinkedHashMap<String, Long> resultado = new LinkedHashMap<>();
        List<String> estados = Arrays.asList(
                "CONFECCIONADA",
                "EN_PROCESO",
                "LISTA",
                "ENTREGADA",
                "CANCELADA"
        );

        Map<String, Long> conteos = cargarRecetas().stream()
                .collect(Collectors.groupingBy(
                        r -> r.getEstado().name(),
                        Collectors.counting()
                ));

        for (String estado : estados) {
            resultado.put(estado, conteos.getOrDefault(estado, 0L));
        }

        return resultado;
    }

    // Estadística: medicamentos por mes (para un medicamento específico)
    public LinkedHashMap<String, Integer> medicamentosPorMes(YearMonth desde, YearMonth hasta) {
        LinkedHashMap<String, Integer> resultado = new LinkedHashMap<>();

        Map<String, Integer> conteos = cargarRecetas().stream()
                .filter(r -> r.getFecha() != null)
                .filter(r -> {
                    YearMonth ym = YearMonth.from(r.getFecha());
                    return !ym.isBefore(desde) && !ym.isAfter(hasta);
                })
                .flatMap(r -> r.getDetalles().stream()
                        .map(d -> Map.entry(YearMonth.from(r.getFecha()).toString(), d.getCantidad()))
                )
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        Integer::sum
                ));

        YearMonth actual = desde;
        while (!actual.isAfter(hasta)) {
            resultado.put(actual.toString(), conteos.getOrDefault(actual.toString(), 0));
            actual = actual.plusMonths(1);
        }

        return resultado;
    }
}
