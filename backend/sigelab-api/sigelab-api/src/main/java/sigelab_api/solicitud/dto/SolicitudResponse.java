package sigelab_api.solicitud.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record SolicitudResponse(

        Long id,

        Long docenteId,
        String docente,

        Long materiaId,
        String materia,

        Long laboratorioId,
        String laboratorio,

        LocalDate fecha,

        LocalTime horaInicio,

        LocalTime horaFin,

        String motivo,

        String estado,

        String observaciones

) {
}