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

    public LinkedHashMap<String, Integer> medicamentosPorMes(YearMonth desde, YearMonth hasta) {
        LinkedHashMap<String, Integer> resultado = new LinkedHashMap<>();

        List<Receta> recetas = cargarRecetas();

        Map<String, Integer> conteos = new HashMap<>();

        for (Receta r : recetas) {
            if (r.getFecha() != null) {
                YearMonth ym = YearMonth.from(r.getFecha());

                if (!ym.isBefore(desde) && !ym.isAfter(hasta)) {
                    String mesKey = ym.toString();

                    if (r.getDetalles() != null && !r.getDetalles().isEmpty()) {
                        int cantidadTotal = r.getDetalles().stream()
                                .mapToInt(detalle->detalle.getCantidad())
                                .sum();

                        conteos.put(mesKey, conteos.getOrDefault(mesKey, 0) + cantidadTotal);
                    }
                }
            }
        }

        YearMonth actual = desde;
        while (!actual.isAfter(hasta)) {
            resultado.put(actual.toString(), conteos.getOrDefault(actual.toString(), 0));
            actual = actual.plusMonths(1);
        }

        return resultado;
    }
}
