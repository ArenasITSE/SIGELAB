package sigelab_api.calendario.dto;

import java.time.LocalTime;

public record OcupacionLaboratorioResponse(

        Long solicitudId,
        String materia,
        String docente,
        LocalTime horaInicio,
        LocalTime horaFin,
        String motivo

) {
}