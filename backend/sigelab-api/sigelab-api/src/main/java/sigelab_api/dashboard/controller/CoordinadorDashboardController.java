package sigelab_api.dashboard.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import sigelab_api.dashboard.dto.CoordinadorDashboardResponse;
import sigelab_api.dashboard.service.CoordinadorDashboardService;

@RestController
@RequestMapping("/api/dashboard/coordinador")
@RequiredArgsConstructor
public class CoordinadorDashboardController {

    private final CoordinadorDashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasRole('COORDINADOR')")
    public CoordinadorDashboardResponse dashboard() {

        return dashboardService.obtenerDashboard();
    }
}