
package sigelab_api.usuario.dto;

import java.util.List;

public record UsuarioResponse(

    Long id,
    String nombre,
    String correo,
    String rol,
    Boolean activo,

    // Carrera principal por compatibilidad
    Long carreraId,
    String carrera,

    // Carreras asignadas
    List<Long> carreraIds,
    List<String> carreras

) {}
