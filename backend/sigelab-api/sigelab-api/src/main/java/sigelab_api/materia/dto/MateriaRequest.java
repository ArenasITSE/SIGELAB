
package sigelab_api.materia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public record MateriaRequest(

        @NotBlank(message = "La clave es obligatoria.")
        String clave,

        @NotBlank(message = "El nombre es obligatorio.")
        String nombre,

        @NotNull(message = "El semestre es obligatorio.")
        @Min(value = 1, message = "El semestre debe ser mayor que cero.")
        @Max(value = 20, message = "El semestre no puede ser mayor a 20.")
        Integer semestre,

        @NotNull(message = "Las horas por semana son obligatorias.")
        @Min(value = 1, message = "Las horas deben ser mayores que cero.")
        Integer horasSemana,

        Long carreraId

) {
}
