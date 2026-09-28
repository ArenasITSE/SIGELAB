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
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public MateriaResponse crear(
            @RequestBody
            @Valid
            MateriaRequest request
    ) {

        return materiaService.crear(request);
    }

    @GetMapping
    public List<MateriaResponse> listar() {

        return materiaService.listar();
    }

    @GetMapping("/{id}")
    public MateriaResponse buscarPorId(
            @PathVariable Long id
    ) {

        return materiaService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public MateriaResponse actualizar(
            @PathVariable Long id,
            @RequestBody
            @Valid
            MateriaRequest request
    ) {

        return materiaService.actualizar(
                id,
                request
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void eliminar(
            @PathVariable Long id
    ) {

        materiaService.eliminar(id);
    }
}