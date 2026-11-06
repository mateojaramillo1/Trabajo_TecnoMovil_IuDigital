import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ProcesadorTransporte {

    //  Contar entradas por estacion durante el dia
    public static Map<String, Long> contarEntradasPorEstacion(List<Registro> registros) {
        return registros.stream()
                .filter(Registro::esEntrada)
                .collect(Collectors.groupingBy(
                        Registro::estacion,
                        Collectors.counting()
                ));
    }

    //  Agrupar registros por hora del dia (0 a 23)
    public static Map<Integer, Long> agruparPorHora(List<Registro> registros) {
        return registros.stream()
                .collect(Collectors.groupingBy(
                        Registro::hora,
                        Collectors.counting()
                ));
    }

    // Identificar las horas pico ordenadas de mayor a menor
    public static List<String> identificarHorasPico(List<Registro> registros, int top) {
        return agruparPorHora(registros).entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(top)
                .map(e -> String.format("Franja %02d:00 a %02d:59 -> %,d registros", e.getKey(), e.getKey(), e.getValue()))
                .toList();
    }

    // determinar las rutas mas utilizadas
    public static List<String> rutasMasUtilizadas(List<Registro> registros, int top) {
        return registros.stream()
                .collect(Collectors.groupingBy(
                        Registro::ruta,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(top)
                .map(e -> String.format("%-26s : %,d validaciones", e.getKey(), e.getValue()))
                .toList();
    }

    // Secuencia de estaciones visitadas por usuario
    public static Map<String, String> patronesDeViajePorUsuario(List<Registro> registros) {
        return registros.stream()
                .sorted(Comparator.comparing(Registro::timestamp))
                .collect(Collectors.groupingBy(
                        Registro::idUsuario,
                        Collectors.mapping(Registro::estacion, Collectors.joining(" -> "))
                ));
    }

    // Tiempo promedio entre estaciones consecutivas
    public static Map<String, Double> tiempoPromedioEntreEstaciones(List<Registro> registros) {
        return IntStream.range(0, registros.size() - 1)
                .filter(i -> registros.get(i).esEntrada()
                        && registros.get(i + 1).esSalida()
                        && registros.get(i).idUsuario().equals(registros.get(i + 1).idUsuario()))
                .mapToObj(i -> {
                    Registro entrada = registros.get(i);
                    Registro salida = registros.get(i + 1);
                    String tramo = entrada.estacion() + " -> " + salida.estacion();
                    long minutos = ChronoUnit.MINUTES.between(entrada.timestamp(), salida.timestamp());
                    return Map.entry(tramo, minutos);
                })
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.averagingLong(Map.Entry::getValue)
                ));
    }

    //Rutas criticas que superan el umbral de ocupacion
    public static List<String> detectarRutasCriticas(List<Registro> registros, long umbral) {
        return registros.stream()
                .filter(Registro::esEntrada)
                .collect(Collectors.groupingBy(
                        Registro::ruta,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .filter(e -> e.getValue() > umbral)
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(e -> e.getKey() + " -> CRITICA (" + e.getValue() + " entradas, supera umbral de " + umbral + ")")
                .toList();
    }

    // Version paralela para medir rendimiento
    public static Map<String, Long> contarEntradasEnParalelo(List<Registro> registros) {
        return registros.parallelStream()
                .filter(Registro::esEntrada)
                .collect(Collectors.groupingByConcurrent(
                        Registro::estacion,
                        Collectors.counting()
                ));
    }
}