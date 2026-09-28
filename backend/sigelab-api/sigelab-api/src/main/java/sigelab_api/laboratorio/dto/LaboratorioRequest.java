package sigelab_api.laboratorio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LaboratorioRequest(

        @NotBlank
        String nombre,

        @NotNull
        Long encargadoId,

        @NotNull
        Long carreraId,

        @NotBlank
        String ubicacion,

        @NotNull
        Integer capacidad

) {
}