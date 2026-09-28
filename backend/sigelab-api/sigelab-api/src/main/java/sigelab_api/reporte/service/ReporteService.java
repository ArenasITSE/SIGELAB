package sigelab_api.reporte.service;

import sigelab_api.reporte.dto.ReporteDocenteResponse;
import sigelab_api.reporte.dto.ReporteOcupacionResponse;
import sigelab_api.reporte.dto.ReporteSolicitudResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReporteService {

    List<ReporteSolicitudResponse> obtenerSolicitudes();

    List<ReporteSolicitudResponse> obtenerSolicitudesPorEstado(
            String estado
    );

    ReporteDocenteResponse obtenerReporteDocente(
            Long docenteId
    );

    List<ReporteOcupacionResponse> obtenerOcupacion(
            LocalDate fecha
    );
}