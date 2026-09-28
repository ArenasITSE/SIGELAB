package sigelab_api.reporte.dto;

public record ReporteDocenteResponse(

        String docente,

        Long totalSolicitudes,

        Long aprobadas,

        Long pendientes,

        Long rechazadas

) {
}