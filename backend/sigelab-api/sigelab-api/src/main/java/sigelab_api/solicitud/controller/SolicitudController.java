
package sigelab_api.solicitud.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import sigelab_api.solicitud.dto.SolicitudRequest;
import sigelab_api.solicitud.dto.SolicitudResponse;
import sigelab_api.solicitud.service.SolicitudService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
@Validated
public class SolicitudController {

    private final SolicitudService solicitudService;

    // ==========================================
    // CREAR SOLICITUD
    // ==========================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('DOCENTE', 'COORDINADOR', 'ADMINISTRADOR')")
    public SolicitudResponse crear(
            @RequestBody @Valid SolicitudRequest request
    ) {
        return solicitudService.crear(request);
    }

    // ==========================================
    // LISTAR SOLICITUDES
    // ==========================================

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCENTE', 'COORDINADOR', 'ADMINISTRADOR')")
    public List<SolicitudResponse> listar() {
        return solicitudService.listar();
    }

    // ==========================================
    // BUSCAR POR ID
    // ==========================================

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCENTE', 'COORDINADOR', 'ADMINISTRADOR')")
    public SolicitudResponse buscarPorId(
            @PathVariable Long id
    ) {
        return solicitudService.buscarPorId(id);
    }

    // ==========================================
    // AUTORIZAR SOLICITUD
    // ==========================================

    @PatchMapping("/{id}/autorizar")
    @PreAuthorize("hasAnyRole('COORDINADOR', 'ADMINISTRADOR')")
    public SolicitudResponse autorizar(
            @PathVariable Long id
    ) {
        return solicitudService.aprobar(id, null);
    }

    // ==========================================
    // RECHAZAR SOLICITUD
    // ==========================================

    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyRole('COORDINADOR', 'ADMINISTRADOR')")
    public SolicitudResponse rechazar(
            @PathVariable Long id,
            @RequestBody @Valid RechazoRequest request
    ) {
        return solicitudService.rechazar(
                id,
                request.observaciones()
        );
    }

    // ==========================================
    // ELIMINAR SOLICITUD
    // ==========================================

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void eliminar(
            @PathVariable Long id
    ) {
        solicitudService.eliminar(id);
    }

    // ==========================================
    // MIS SOLICITUDES
    // ==========================================

    @GetMapping("/mis-solicitudes")
    @PreAuthorize("hasRole('DOCENTE')")
    public List<SolicitudResponse> misSolicitudes() {
        return solicitudService.listarMisSolicitudes();
    }

    // ==========================================
    // DTO PARA RECHAZO
    // ==========================================

    public record RechazoRequest(

            @NotBlank(message = "Debes indicar el motivo del rechazo.")
            String observaciones

    ) {
    }

    // ==========================================
    // MANEJO DE ERRORES
    // ==========================================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> manejarAccesoDenegado(
            AccessDeniedException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "mensaje",
                        obtenerMensaje(ex, "No tienes permisos para realizar esta operación.")
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarArgumentos(
            IllegalArgumentException ex
    ) {
        return ResponseEntity
                .badRequest()
                .body(Map.of(
                        "mensaje",
                        obtenerMensaje(ex, "Los datos enviados no son válidos.")
                ));
    }

    private String obtenerMensaje(Exception ex, String mensajeDefecto) {
        return ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage()
                : mensajeDefecto;
    }
}
