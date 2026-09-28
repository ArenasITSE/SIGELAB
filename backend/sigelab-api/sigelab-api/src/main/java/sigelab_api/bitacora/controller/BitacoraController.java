package sigelab_api.bitacora.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import sigelab_api.bitacora.dto.BitacoraRequest;
import sigelab_api.bitacora.dto.BitacoraResponse;
import sigelab_api.bitacora.service.BitacoraService;

import java.util.List;

@RestController
@RequestMapping("/api/bitacoras")
@RequiredArgsConstructor
public class BitacoraController {

    private final BitacoraService bitacoraService;

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCENTE','ADMINISTRADOR')")
    public BitacoraResponse crear(
            @Valid
            @RequestBody
            BitacoraRequest request
    ) {

        return bitacoraService.crear(request);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<BitacoraResponse> listar() {

        return bitacoraService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public BitacoraResponse buscarPorId(
            @PathVariable Long id
    ) {

        return bitacoraService.buscarPorId(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void eliminar(
            @PathVariable Long id
    ) {

        bitacoraService.eliminar(id);
    }
}