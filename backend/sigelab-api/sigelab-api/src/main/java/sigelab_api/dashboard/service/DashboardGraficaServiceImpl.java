package sigelab_api.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import sigelab_api.dashboard.dto.EstadisticaGeneralResponse;
import sigelab_api.dashboard.dto.LaboratorioUsoResponse;
import sigelab_api.dashboard.dto.SolicitudesMesResponse;

import sigelab_api.solicitud.entity.EstadoSolicitud;
import sigelab_api.solicitud.repository.SolicitudLaboratorioRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardGraficaServiceImpl
        implements DashboardGraficaService {

    private final SolicitudLaboratorioRepository repository;

    @Override
    public EstadisticaGeneralResponse obtenerEstados() {

        Long aprobadas = 0L;
        Long pendientes = 0L;
        Long rechazadas = 0L;

        List<Object[]> datos =
                repository.contarPorEstado();

        for (Object[] fila : datos) {

            EstadoSolicitud estado =
                    (EstadoSolicitud) fila[0];

            Long total =
                    ((Number) fila[1]).longValue();

            switch (estado) {

                case APROBADA ->
                        aprobadas = total;

                case PENDIENTE ->
                        pendientes = total;

                case RECHAZADA ->
                        rechazadas = total;
            }
        }

        return EstadisticaGeneralResponse
                .builder()
                .aprobadas(aprobadas)
                .pendientes(pendientes)
                .rechazadas(rechazadas)
                .build();
    }

    @Override
    public List<LaboratorioUsoResponse>
    laboratoriosMasUsados() {

        List<Object[]> datos =
                repository.laboratoriosMasUsados();

        List<LaboratorioUsoResponse> respuesta =
                new ArrayList<>();

        for (Object[] fila : datos) {

            respuesta.add(
                    new LaboratorioUsoResponse(
                            fila[0].toString(),
                            ((Number) fila[1]).longValue()
                    )
            );
        }

        return respuesta;
    }

    @Override
    public List<SolicitudesMesResponse>
    solicitudesPorMes() {

        List<Object[]> datos =
                repository.solicitudesPorMes();

        List<SolicitudesMesResponse> respuesta =
                new ArrayList<>();

        String[] meses = {
                "",
                "Enero",
                "Febrero",
                "Marzo",
                "Abril",
                "Mayo",
                "Junio",
                "Julio",
                "Agosto",
                "Septiembre",
                "Octubre",
                "Noviembre",
                "Diciembre"
        };

        for (Object[] fila : datos) {

            Integer mes =
                    ((Number) fila[0]).intValue();

            Long total =
                    ((Number) fila[1]).longValue();

            respuesta.add(
                    new SolicitudesMesResponse(
                            meses[mes],
                            total
                    )
            );
        }

        return respuesta;
    }
}