package sigelab_api.carrera.dto;

import jakarta.validation.constraints.NotBlank;

public record CarreraRequest(

    @NotBlank(message = "La clave es obligatoria")
    String clave,

    @NotBlank(message = "El nombre es obligatorio")
    String nombre

) {}