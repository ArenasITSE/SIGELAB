
package sigelab_api.laboratorio.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import sigelab_api.laboratorio.dto.LaboratorioRequest;
import sigelab_api.laboratorio.dto.LaboratorioResponse;
import sigelab_api.laboratorio.service.LaboratorioService;

import java.util.List;

@RestController
@RequestMapping("/api/laboratorios")
@RequiredArgsConstructor
public class LaboratorioController {

    private final LaboratorioService laboratorioService;

    // REGISTRAR LABORATORIO
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_COORDINADOR')")
    public LaboratorioResponse crear(
            @RequestBody @Valid LaboratorioRequest request) {

        return laboratorioService.crear(request);
    }

    // LISTAR LABORATORIOS
    @GetMapping
    public List<LaboratorioResponse> listar() {
        return laboratorioService.listar();
    }

    // BUSCAR POR ID
    @GetMapping("/{id}")
    public LaboratorioResponse buscarPorId(
            @PathVariable Long id) {

        return laboratorioService.buscarPorId(id);
    }

    // ACTUALIZAR LABORATORIO
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_COORDINADOR')")
    public LaboratorioResponse actualizar(
            @PathVariable Long id,
            @RequestBody @Valid LaboratorioRequest request) {

        return laboratorioService.actualizar(id, request);
    }

    // DESACTIVAR LABORATORIO (ELIMINACIÓN LÓGICA)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_COORDINADOR')")
    public void eliminar(@PathVariable Long id) {
        laboratorioService.eliminar(id);
    }

    // ELIMINAR DEFINITIVAMENTE (SOLO ADMINISTRADOR)
    @DeleteMapping("/{id}/permanente")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarDefinitivamente(
            @PathVariable Long id) {

        laboratorioService.eliminarDefinitivamente(id);

        return ResponseEntity.noContent().build();
    }
}
