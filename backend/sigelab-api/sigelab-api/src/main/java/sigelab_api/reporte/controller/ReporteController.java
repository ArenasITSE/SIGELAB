package sigelab_api.reporte.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import sigelab_api.reporte.dto.ReporteDocenteResponse;
import sigelab_api.reporte.dto.ReporteOcupacionResponse;
import sigelab_api.reporte.dto.ReporteSolicitudResponse;
import sigelab_api.reporte.service.ReporteService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/solicitudes")
    @PreAuthorize("hasAnyRole('COORDINADOR','ADMINISTRADOR')")
    public List<ReporteSolicitudResponse> solicitudes(
            @RequestParam(required = false)
            String estado
    ) {

        if (estado != null) {
            return reporteService
                    .obtenerSolicitudesPorEstado(
                            estado
                    );
        }

        return reporteService.obtenerSolicitudes();
    }

    @GetMapping("/docente/{docenteId}")
    @PreAuthorize("hasAnyRole('COORDINADOR','ADMINISTRADOR')")
    public ReporteDocenteResponse docente(
            @PathVariable Long docenteId
    ) {

        return reporteService
                .obtenerReporteDocente(
                        docenteId
                );
    }

    @GetMapping("/ocupacion")
    @PreAuthorize("hasAnyRole('COORDINADOR','ADMINISTRADOR')")
    public List<ReporteOcupacionResponse> ocupacion(
            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fecha
    ) {

        return reporteService
                .obtenerOcupacion(
                        fecha
                );
    }
}