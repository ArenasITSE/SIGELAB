package sigelab_api.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import sigelab_api.dashboard.dto.CoordinadorDashboardResponse;
import sigelab_api.solicitud.entity.EstadoSolicitud;
import sigelab_api.solicitud.repository.SolicitudLaboratorioRepository;

@Service
@RequiredArgsConstructor
public class CoordinadorDashboardServiceImpl
        implements CoordinadorDashboardService {

    private final SolicitudLaboratorioRepository solicitudRepository;

    @Override
    public CoordinadorDashboardResponse obtenerDashboard() {

        long pendientes =
                solicitudRepository.countByEstado(
                        EstadoSolicitud.PENDIENTE
                );

        long aprobadas =
                solicitudRepository.countByEstado(
                        EstadoSolicitud.APROBADA
                );

        long rechazadas =
                solicitudRepository.countByEstado(
                        EstadoSolicitud.RECHAZADA
                );

        return new CoordinadorDashboardResponse(
                pendientes,
                aprobadas,
                rechazadas
        );
    }
}