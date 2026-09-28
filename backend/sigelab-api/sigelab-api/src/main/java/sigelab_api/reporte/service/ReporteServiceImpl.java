package sigelab_api.reporte.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import sigelab_api.reporte.dto.ReporteDocenteResponse;
import sigelab_api.reporte.dto.ReporteOcupacionResponse;
import sigelab_api.reporte.dto.ReporteSolicitudResponse;

import sigelab_api.solicitud.entity.EstadoSolicitud;
import sigelab_api.solicitud.entity.SolicitudLaboratorio;
import sigelab_api.solicitud.repository.SolicitudLaboratorioRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private final SolicitudLaboratorioRepository solicitudRepository;

    @Override
    public List<ReporteSolicitudResponse> obtenerSolicitudes() {

        return solicitudRepository.findAll()
                .stream()
                .map(this::mapToReporteSolicitud)
                .toList();
    }

    @Override
    public List<ReporteSolicitudResponse> obtenerSolicitudesPorEstado(
            String estado
    ) {

        EstadoSolicitud estadoSolicitud =
                EstadoSolicitud.valueOf(
                        estado.toUpperCase()
                );

        return solicitudRepository.findAll()
                .stream()
                .filter(
                        s -> s.getEstado()
                                .equals(estadoSolicitud)
                )
                .map(this::mapToReporteSolicitud)
                .toList();
    }

    @Override
    public ReporteDocenteResponse obtenerReporteDocente(
            Long docenteId
    ) {

        List<SolicitudLaboratorio> solicitudes =
                solicitudRepository.findByDocenteId(
                        docenteId
                );

        long aprobadas =
                solicitudes.stream()
                        .filter(
                                s -> s.getEstado()
                                        == EstadoSolicitud.APROBADA
                        )
                        .count();

        long pendientes =
                solicitudes.stream()
                        .filter(
                                s -> s.getEstado()
                                        == EstadoSolicitud.PENDIENTE
                        )
                        .count();

        long rechazadas =
                solicitudes.stream()
                        .filter(
                                s -> s.getEstado()
                                        == EstadoSolicitud.RECHAZADA
                        )
                        .count();

        String docente =
                solicitudes.isEmpty()
                        ? "Sin solicitudes"
                        : solicitudes.get(0)
                                .getDocente()
                                .getNombre();

        return new ReporteDocenteResponse(
                docente,
                (long) solicitudes.size(),
                aprobadas,
                pendientes,
                rechazadas
        );
    }

    @Override
    public List<ReporteOcupacionResponse> obtenerOcupacion(
            LocalDate fecha
    ) {

        return solicitudRepository.findAll()
                .stream()
                .filter(
                        s -> s.getFecha()
                                .equals(fecha)
                )
                .collect(
                        java.util.stream.Collectors.groupingBy(
                                s -> s.getLaboratorio()
                                        .getNombre(),
                                java.util.stream.Collectors.counting()
                        )
                )
                .entrySet()
                .stream()
                .map(
                        e -> new ReporteOcupacionResponse(
                                e.getKey(),
                                e.getValue()
                        )
                )
                .toList();
    }

    private ReporteSolicitudResponse mapToReporteSolicitud(
            SolicitudLaboratorio solicitud
    ) {

        return new ReporteSolicitudResponse(
                solicitud.getId(),
                solicitud.getDocente().getNombre(),
                solicitud.getMateria().getNombre(),
                solicitud.getLaboratorio().getNombre(),
                solicitud.getFecha(),
                solicitud.getEstado().name()
        );
    }
}