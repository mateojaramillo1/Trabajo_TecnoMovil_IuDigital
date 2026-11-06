import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GeneradorDatos {

    private static final LocalDate FECHA_SIMULACION = LocalDate.of(2026, 10, 26);

    public static final List<String> RUTAS = List.of(
            "R1-Troncal Norte",
            "R2-Troncal Sur",
            "R3-Alimentador Oriente",
            "R4-Alimentador Occidente",
            "R5-Circular Centro"
    );

    public static final Map<String, String[]> ESTACIONES_POR_RUTA = Map.of(
            "R1-Troncal Norte", new String[]{"Portal Norte", "La Ceiba", "San Jose", "Estacion Central"},
            "R2-Troncal Sur", new String[]{"Portal Sur", "El Mirador", "Las Palmas", "Estacion Central"},
            "R3-Alimentador Oriente", new String[]{"Loma Verde", "El Rosal", "San Jose"},
            "R4-Alimentador Occidente", new String[]{"Villa Luz", "El Prado", "Las Palmas"},
            "R5-Circular Centro", new String[]{"Estacion Central", "Plaza Mayor", "Estadio", "Universidad"}
    );

    private static final int[] HORAS_FRECUENTES = {
            6, 7, 7, 8, 8, 12, 13, 16, 17, 17, 18, 18, 19, 9, 10, 14, 15, 20
    };

    public static List<Registro> generar(int cantidadUsuarios, long semilla) {
        Random aleatorio = new Random(semilla);
        return IntStream.rangeClosed(1, cantidadUsuarios)
                .boxed()
                .flatMap(indice -> simularViajesUsuario(indice, aleatorio))
                .toList();
    }

    private static Stream<Registro> simularViajesUsuario(int indice, Random aleatorio) {
        String idUsuario = "U-" + indice;
        List<Registro> eventos = new ArrayList<>(4);

        for (int viaje = 0; viaje < 2; viaje++) {
            int indiceRuta = (aleatorio.nextInt(10) < 5)
                    ? aleatorio.nextInt(2)
                    : aleatorio.nextInt(RUTAS.size());

            String ruta = RUTAS.get(indiceRuta);
            String[] estaciones = ESTACIONES_POR_RUTA.get(ruta);

            int posOrigen = aleatorio.nextInt(estaciones.length - 1);
            String estacionEntrada = estaciones[posOrigen];
            String estacionSalida = estaciones[posOrigen + 1];

            int hora = HORAS_FRECUENTES[aleatorio.nextInt(HORAS_FRECUENTES.length)];
            int minuto = aleatorio.nextInt(40);
            int duracionMinutos = 8 + aleatorio.nextInt(15);

            LocalDateTime momentoEntrada = FECHA_SIMULACION.atTime(hora, minuto);
            LocalDateTime momentoSalida = momentoEntrada.plusMinutes(duracionMinutos);

            eventos.add(new Registro(idUsuario, ruta, estacionEntrada, "entrada", momentoEntrada));
            eventos.add(new Registro(idUsuario, ruta, estacionSalida, "salida", momentoSalida));
        }

        return eventos.stream();
    }
}