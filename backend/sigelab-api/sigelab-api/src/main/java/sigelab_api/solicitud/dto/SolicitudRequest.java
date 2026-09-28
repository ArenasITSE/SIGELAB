package sigelab_api.solicitud.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record SolicitudRequest(

        @NotNull
        Long materiaId,

        @NotNull
        Long laboratorioId,

        @NotNull
        LocalDate fecha,

        @NotNull
        LocalTime horaInicio,

        @NotNull
        LocalTime horaFin,

        @Size(min = 5, max = 500)
        String motivo

) {
}