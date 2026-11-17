
package sigelab_api.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import sigelab_api.usuario.entity.Rol;

public record UsuarioRequest(

    @NotBlank(message = "El nombre es obligatorio")
    String nombre,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    String correo,

    String password,

    @NotNull(message = "El rol es obligatorio")
    Rol rol,

    Long carreraId

) {}
