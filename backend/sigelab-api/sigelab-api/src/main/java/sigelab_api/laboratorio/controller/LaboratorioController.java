package sigelab_api.laboratorio.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR')")
    public LaboratorioResponse crear(
            @RequestBody
            @Valid
            LaboratorioRequest request
    ) {

        return laboratorioService.crear(request);
    }

    @GetMapping
    public List<LaboratorioResponse> listar() {

        return laboratorioService.listar();
    }

    @GetMapping("/{id}")
    public LaboratorioResponse buscarPorId(
            @PathVariable Long id
    ) {

        return laboratorioService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR')")
    public LaboratorioResponse actualizar(
            @PathVariable Long id,
            @RequestBody
            @Valid
            LaboratorioRequest request
    ) {

        return laboratorioService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR')")
    public void eliminar(
            @PathVariable Long id
    ) {

        laboratorioService.eliminar(id);
    }
}