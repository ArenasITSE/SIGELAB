
package sigelab_api.materia.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import sigelab_api.materia.dto.MateriaRequest;
import sigelab_api.materia.dto.MateriaResponse;
import sigelab_api.materia.service.MateriaService;

import java.util.List;

@RestController
@RequestMapping("/api/materias")
@RequiredArgsConstructor
public class MateriaController {

    private final MateriaService materiaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR')")
    public MateriaResponse crear(
            @RequestBody @Valid MateriaRequest request) {
        return materiaService.crear(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'DOCENTE')")
    public List<MateriaResponse> listar() {
        return materiaService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'DOCENTE')")
    public MateriaResponse buscarPorId(@PathVariable Long id) {
        return materiaService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR')")
    public MateriaResponse actualizar(
            @PathVariable Long id,
            @RequestBody @Valid MateriaRequest request) {
        return materiaService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR')")
    public void eliminar(@PathVariable Long id) {
        materiaService.eliminar(id);
    }

    // REACTIVAR MATERIA
    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR')")
    public void activar(@PathVariable Long id) {
        materiaService.activar(id);
    }
}
