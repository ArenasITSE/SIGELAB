package sigelab_api.dashboard.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import sigelab_api.dashboard.dto.DocenteDashboardResponse;
import sigelab_api.dashboard.service.DocenteDashboardService;
import sigelab_api.usuario.entity.Usuario;
import sigelab_api.usuario.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/dashboard/docente")
@RequiredArgsConstructor
public class DocenteDashboardController {

    private final DocenteDashboardService dashboardService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasRole('DOCENTE')")
    public DocenteDashboardResponse obtenerDashboard(
            Authentication authentication
    ) {

        String correo = authentication.getName();

        Usuario usuario = usuarioRepository
                .findByCorreo(correo)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuario no encontrado"
                        ));

        return dashboardService.obtenerDashboard(
                usuario.getId()
        );
    }
}