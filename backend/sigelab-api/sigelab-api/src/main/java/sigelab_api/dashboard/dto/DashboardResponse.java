package sigelab_api.dashboard.dto;

public record DashboardResponse(

        Long usuarios,
        Long docentes,
        Long coordinadores,
        Long administradores,

        Long laboratorios,
        Long materias,

        Long solicitudesPendientes,
        Long solicitudesAprobadas,
        Long solicitudesRechazadas

) {
}