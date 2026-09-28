package sigelab_api.dashboard.dto;

public record CoordinadorDashboardResponse(

        long pendientes,
        long aprobadas,
        long rechazadas

) {
}