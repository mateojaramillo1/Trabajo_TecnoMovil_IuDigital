import java.time.LocalDateTime;


public record Registro(
        String idUsuario,
        String ruta,
        String estacion,
        String accion,          // "entrada" o "salida"
        LocalDateTime timestamp
) {
    public boolean esEntrada() {
        return "entrada".equalsIgnoreCase(accion);
    }

    public boolean esSalida() {
        return "salida".equalsIgnoreCase(accion);
    }

    public int hora() {
        return timestamp.getHour();
    }
}