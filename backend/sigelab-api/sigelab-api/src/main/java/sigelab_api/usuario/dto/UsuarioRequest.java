
package sigelab_api.usuario.dto;

import sigelab_api.usuario.entity.Rol;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UsuarioRequest(

    @NotBlank(message = "El nombre es obligatorio")
    String nombre,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    String correo,

    String password,

    @NotNull(message = "El rol es obligatorio")
    Rol rol,

    // Se conserva para coordinadores, técnicos y administradores
    Long carreraId,

    // Se utiliza para asignar varias carreras a docentes
    List<Long> carreraIds

) {}
