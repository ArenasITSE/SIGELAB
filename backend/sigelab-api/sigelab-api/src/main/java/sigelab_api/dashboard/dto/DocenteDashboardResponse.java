package sigelab_api.dashboard.dto;

public record DocenteDashboardResponse(

        long misSolicitudes,
        long pendientes,
        long aprobadas,
        long rechazadas

) {
}