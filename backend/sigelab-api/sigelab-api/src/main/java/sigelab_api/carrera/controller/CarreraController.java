package sigelab_api.carrera.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import sigelab_api.carrera.dto.CarreraRequest;
import sigelab_api.carrera.dto.CarreraResponse;
import sigelab_api.carrera.service.CarreraService;

import java.util.List;

@RestController
@RequestMapping("/api/carreras")
@RequiredArgsConstructor
public class CarreraController {

    private final CarreraService carreraService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public CarreraResponse crear(
            @RequestBody
            @Valid
            CarreraRequest request
    ) {

        return carreraService.crear(request);
    }

    @GetMapping
    public List<CarreraResponse> listar() {

        return carreraService.listar();
    }

    @GetMapping("/{id}")
    public CarreraResponse buscarPorId(
            @PathVariable Long id
    ) {

        return carreraService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public CarreraResponse actualizar(
            @PathVariable Long id,
            @RequestBody
            @Valid
            CarreraRequest request
    ) {

        return carreraService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void eliminar(
            @PathVariable Long id
    ) {

        carreraService.eliminar(id);
    }
}