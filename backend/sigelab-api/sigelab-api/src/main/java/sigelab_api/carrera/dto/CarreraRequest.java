package sigelab_api.carrera.dto;

import jakarta.validation.constraints.NotBlank;

public record CarreraRequest(

        @NotBlank
        String clave,

        @NotBlank
        String nombre

) {
}