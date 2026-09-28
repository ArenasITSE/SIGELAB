package sigelab_api.materia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MateriaRequest(

        @NotBlank
        String clave,

        @NotBlank
        String nombre,

        @NotNull
        Integer semestre,

        @NotNull
        Integer horasSemana,

        @NotNull
        Long carreraId

) {
}