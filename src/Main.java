import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Main {

    public static final long SEMILLA = 2026L;
    public static final int CANTIDAD_USUARIOS = 250_000;
    public static final long UMBRAL_OCUPACION = 120_000L;

    public static void main(String[] args) {

        titulo("PRUEBA CON DATOS PEQUENOS Y VERIFICACION DE INMUTABILIDAD");
        List<Registro> datosPrueba = List.of(
                new Registro("U-1", "R1-Troncal Norte", "Portal Norte", "entrada", LocalDateTime.of(2026, 10, 26, 7, 0)),
                new Registro("U-1", "R1-Troncal Norte", "La Ceiba",     "salida",  LocalDateTime.of(2026, 10, 26, 7, 15)),
                new Registro("U-2", "R1-Troncal Norte", "Portal Norte", "entrada", LocalDateTime.of(2026, 10, 26, 7, 10)),
                new Registro("U-2", "R1-Troncal Norte", "La Ceiba",     "salida",  LocalDateTime.of(2026, 10, 26, 7, 35))
        );

        System.out.println("Entradas en prueba pequena     : " + ProcesadorTransporte.contarEntradasPorEstacion(datosPrueba));
        System.out.println("Secuencia de estaciones de U-1 : " + ProcesadorTransporte.patronesDeViajePorUsuario(datosPrueba).get("U-1"));
        System.out.println("Tiempo promedio (esperado 20m) : " + ProcesadorTransporte.tiempoPromedioEntreEstaciones(datosPrueba));
        System.out.println("Ruta critica (umbral > 1)      : " + ProcesadorTransporte.detectarRutasCriticas(datosPrueba, 1L));
        System.out.println("Tamano lista original intacto  : " + datosPrueba.size() + " registros (Inmutabilidad OK)");

        titulo("GENERADOR 1.000.000 DE REGISTROS SIMULADOS (SEMILLA = " + SEMILLA + ")");
        long inicioGen = System.currentTimeMillis();
        List<Registro> registros = GeneradorDatos.generar(CANTIDAD_USUARIOS, SEMILLA);
        long tiempoGen = System.currentTimeMillis() - inicioGen;
        System.out.printf("Total generados: %,d registros en %d ms%n", registros.size(), tiempoGen);

        titulo("CONTAR ENTRADAS POR ESTACION DURANTE EL DIA");
        Map<String, Long> entradasPorEstacion = ProcesadorTransporte.contarEntradasPorEstacion(registros);
        entradasPorEstacion.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(e -> System.out.printf("   %-20s : %,d entradas%n", e.getKey(), e.getValue()));

        titulo("AGRUPAR POR HORA E IDENTIFICAR HORAS PICO (TOP 5)");
        ProcesadorTransporte.identificarHorasPico(registros, 5)
                .forEach(linea -> System.out.println("   " + linea));

        titulo("DETERMINAR LAS RUTAS MAS UTILIZADAS");
        ProcesadorTransporte.rutasMasUtilizadas(registros, 5)
                .forEach(linea -> System.out.println("   " + linea));

        titulo("SECUENCIA DE ESTACIONES POR USUARIO ");
        Map<String, String> patrones = ProcesadorTransporte.patronesDeViajePorUsuario(registros);
        patrones.entrySet().stream()
                .limit(4)
                .forEach(e -> System.out.println("   " + e.getKey() + " -> " + e.getValue()));

        titulo("TIEMPO PROMEDIO ENTRE ESTACIONES CONSECUTIVAS ");
        Map<String, Double> tiemposPromedio = ProcesadorTransporte.tiempoPromedioEntreEstaciones(registros);
        tiemposPromedio.entrySet().stream()
                .limit(5)
                .forEach(e -> System.out.printf("   %-34s : %.2f minutos%n", e.getKey(), e.getValue()));

        titulo("RUTAS CRITICAS (SUPERAN UMBRAL DE " + String.format("%,d", UMBRAL_OCUPACION) + " ENTRADAS)");
        ProcesadorTransporte.detectarRutasCriticas(registros, UMBRAL_OCUPACION)
                .forEach(alerta -> System.out.println("   " + alerta));

        titulo("COMPARATIVA DE TIEMPOS: STREAM() SECUENCIAL VS PARALLELSTREAM()");
        long t1 = System.currentTimeMillis();
        Map<String, Long> resSecuencial = ProcesadorTransporte.contarEntradasPorEstacion(registros);
        long msSecuencial = System.currentTimeMillis() - t1;

        long t2 = System.currentTimeMillis();
        Map<String, Long> resParalelo = ProcesadorTransporte.contarEntradasEnParalelo(registros);
        long msParalelo = System.currentTimeMillis() - t2;

        System.out.println("   Tiempo con stream() secuencial       : " + msSecuencial + " ms");
        System.out.println("   Tiempo con parallelStream() paralelo : " + msParalelo + " ms");
        System.out.println("   Conteos identicos sin errores        : " + resSecuencial.equals(resParalelo));
        System.out.println("   Tamano de lista original sin cambios : " + registros.size() + " registros");
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println(texto);
        System.out.println("-".repeat(texto.length()));
    }
}