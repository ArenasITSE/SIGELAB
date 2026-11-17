package sigelab_api.usuario.dto;

import java.util.List;

public record ResultadoImportacion(
    int total,
    int exitosos,
    int rechazados,
    List<DetalleImportacion> detalles
) {

    public record DetalleImportacion(
        int fila,
        String correo,
        String estado,
        String mensaje
    ) {}
}