package sigelab_api.reporte.dto;

import java.time.LocalDate;

public record ReporteSolicitudResponse(

        Long id,

        String docente,

        String materia,

        String laboratorio,

        LocalDate fecha,

        String estado

) {
}