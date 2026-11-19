
package sigelab_api.usuario.dto;

public record CarreraUsuarioResponse(
    Long id,
    String clave,
    String nombre,
    Boolean activo
) {}
