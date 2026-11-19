
package sigelab_api.solicitud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record SolicitudRequest(

        @NotNull(message = "Debes seleccionar una materia.")
        Long materiaId,

        @NotNull(message = "Debes seleccionar un laboratorio.")
        Long laboratorioId,

        @NotNull(message = "La fecha es obligatoria.")
        LocalDate fecha,

        @NotNull(message = "La hora de inicio es obligatoria.")
        LocalTime horaInicio,

        @NotNull(message = "La hora de finalización es obligatoria.")
        LocalTime horaFin,

        @NotBlank(message = "El motivo es obligatorio.")
        @Size(min = 5, max = 500,
              message = "El motivo debe tener entre 5 y 500 caracteres.")
        String motivo

) {
}
