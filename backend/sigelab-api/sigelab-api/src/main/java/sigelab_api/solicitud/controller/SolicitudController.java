package sigelab_api.solicitud.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import sigelab_api.solicitud.dto.SolicitudRequest;
import sigelab_api.solicitud.dto.SolicitudResponse;
import sigelab_api.solicitud.service.SolicitudService;

import sigelab_api.usuario.entity.Usuario;
import sigelab_api.usuario.repository.UsuarioRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCENTE','ADMINISTRADOR')")
    public SolicitudResponse crear(
            @RequestBody
            @Valid
            SolicitudRequest request,
            Authentication authentication
    ) {

        return solicitudService.crear(
                request,
                authentication.getName()
        );
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<SolicitudResponse> listar() {
        return solicitudService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public SolicitudResponse buscarPorId(
            @PathVariable Long id
    ) {
        return solicitudService.buscarPorId(id);
    }

    @PutMapping("/{id}/aprobar")
    @PreAuthorize("hasAnyRole('COORDINADOR','ADMINISTRADOR')")
    public SolicitudResponse aprobar(
            @PathVariable Long id,
            @RequestParam String observaciones
    ) {
        return solicitudService.aprobar(
                id,
                observaciones
        );
    }

    @PutMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyRole('COORDINADOR','ADMINISTRADOR')")
    public SolicitudResponse rechazar(
            @PathVariable Long id,
            @RequestParam String observaciones
    ) {
        return solicitudService.rechazar(
                id,
                observaciones
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void eliminar(
            @PathVariable Long id
    ) {
        solicitudService.eliminar(id);
    }

    @GetMapping("/mis-solicitudes")
    @PreAuthorize("hasRole('DOCENTE')")
    public List<SolicitudResponse> misSolicitudes(
            Authentication authentication
    ) {

        String correo = authentication.getName();

        Usuario usuario = usuarioRepository
                .findByCorreo(correo)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        return solicitudService.listarMisSolicitudes(
                usuario.getId()
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> manejarError(
            RuntimeException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        Map.of(
                                "mensaje",
                                ex.getMessage()
                        )
                );
    }
}