package sigelab_api.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import sigelab_api.usuario.entity.Rol;

public record UsuarioRequest(

        @NotBlank
        String nombre,

        @Email
        @NotBlank
        String correo,

        @NotBlank
        String password,

        @NotNull
        Rol rol

) {
}