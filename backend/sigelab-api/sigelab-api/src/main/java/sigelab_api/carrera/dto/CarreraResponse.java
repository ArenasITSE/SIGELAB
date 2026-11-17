package sigelab_api.carrera.dto;

public record CarreraResponse(
    Long id,
    String clave,
    String nombre,
    Boolean activo
) {}