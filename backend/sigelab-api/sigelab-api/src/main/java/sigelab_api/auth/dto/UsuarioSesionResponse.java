
package sigelab_api.auth.dto;

public record UsuarioSesionResponse(
        Long id,
        String nombre,
        String correo,
        String rol,
        Boolean activo,
        Long carreraId,
        String carrera
) {
}
