package sigelab_api.bitacora.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record BitacoraRequest(

        @NotNull
        Long solicitudId,

        @NotNull
        LocalTime horaEntrada,

        @NotNull
        LocalTime horaSalida,

        String observaciones,

        String incidencias

) {
}