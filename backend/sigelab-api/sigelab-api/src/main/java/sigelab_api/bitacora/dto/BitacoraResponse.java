package sigelab_api.bitacora.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record BitacoraResponse(

        Long id,

        Long solicitudId,

        String docente,

        String materia,

        String laboratorio,

        LocalDate fecha,

        LocalTime horaEntrada,

        LocalTime horaSalida,

        String observaciones,

        String incidencias

) {
}