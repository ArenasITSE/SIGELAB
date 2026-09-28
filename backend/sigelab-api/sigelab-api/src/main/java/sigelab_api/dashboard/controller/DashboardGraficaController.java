package sigelab_api.dashboard.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import sigelab_api.dashboard.dto.EstadisticaGeneralResponse;
import sigelab_api.dashboard.dto.LaboratorioUsoResponse;
import sigelab_api.dashboard.dto.SolicitudesMesResponse;
import sigelab_api.dashboard.service.DashboardGraficaService;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard/graficas")
@RequiredArgsConstructor
public class DashboardGraficaController {

    private final DashboardGraficaService service;

    @GetMapping("/estados")
    @PreAuthorize("isAuthenticated()")
    public EstadisticaGeneralResponse estados() {

        return service.obtenerEstados();
    }

    @GetMapping("/laboratorios")
    @PreAuthorize("isAuthenticated()")
    public List<LaboratorioUsoResponse> laboratorios() {

        return service.laboratoriosMasUsados();
    }

    @GetMapping("/mensual")
    @PreAuthorize("isAuthenticated()")
    public List<SolicitudesMesResponse> mensual() {

        return service.solicitudesPorMes();
    }
}