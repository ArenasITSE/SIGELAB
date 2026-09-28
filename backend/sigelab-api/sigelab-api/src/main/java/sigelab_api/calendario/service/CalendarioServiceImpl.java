package sigelab_api.calendario.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import sigelab_api.calendario.dto.OcupacionLaboratorioResponse;
import sigelab_api.solicitud.entity.EstadoSolicitud;
import sigelab_api.solicitud.entity.SolicitudLaboratorio;
import sigelab_api.solicitud.repository.SolicitudLaboratorioRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarioServiceImpl
        implements CalendarioService {

    private final SolicitudLaboratorioRepository solicitudRepository;

    @Override
    public List<OcupacionLaboratorioResponse> obtenerOcupacion(
            Long laboratorioId,
            LocalDate fecha
    ) {

        List<SolicitudLaboratorio> solicitudes =
                solicitudRepository
                        .findByLaboratorioIdAndFechaAndEstado(
                                laboratorioId,
                                fecha,
                                EstadoSolicitud.APROBADA
                        );

        return solicitudes.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private OcupacionLaboratorioResponse mapToResponse(
            SolicitudLaboratorio solicitud
    ) {

        return new OcupacionLaboratorioResponse(
                solicitud.getId(),
                solicitud.getMateria().getNombre(),
                solicitud.getDocente().getNombre(),
                solicitud.getHoraInicio(),
                solicitud.getHoraFin(),
                solicitud.getMotivo()
        );
    }
}