package sigelab_api.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import sigelab_api.dashboard.dto.DocenteDashboardResponse;
import sigelab_api.solicitud.entity.EstadoSolicitud;
import sigelab_api.solicitud.repository.SolicitudLaboratorioRepository;

@Service
@RequiredArgsConstructor
public class DocenteDashboardServiceImpl
        implements DocenteDashboardService {

    private final SolicitudLaboratorioRepository solicitudRepository;

    @Override
    public DocenteDashboardResponse obtenerDashboard(
            Long docenteId
    ) {

        long total =
                solicitudRepository.countByDocenteId(
                        docenteId
                );

        long pendientes =
                solicitudRepository.countByDocenteIdAndEstado(
                        docenteId,
                        EstadoSolicitud.PENDIENTE
                );

        long aprobadas =
                solicitudRepository.countByDocenteIdAndEstado(
                        docenteId,
                        EstadoSolicitud.APROBADA
                );

        long rechazadas =
                solicitudRepository.countByDocenteIdAndEstado(
                        docenteId,
                        EstadoSolicitud.RECHAZADA
                );

        return new DocenteDashboardResponse(
                total,
                pendientes,
                aprobadas,
                rechazadas
        );
    }
}