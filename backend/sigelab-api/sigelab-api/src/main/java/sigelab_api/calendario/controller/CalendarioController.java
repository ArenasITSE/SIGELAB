package sigelab_api.calendario.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import sigelab_api.calendario.dto.OcupacionLaboratorioResponse;
import sigelab_api.calendario.service.CalendarioService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendario")
@RequiredArgsConstructor
public class CalendarioController {

    private final CalendarioService calendarioService;

    @GetMapping("/laboratorio/{laboratorioId}")
    @PreAuthorize("isAuthenticated()")
    public List<OcupacionLaboratorioResponse> obtenerOcupacion(

            @PathVariable Long laboratorioId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha

    ) {

        return calendarioService.obtenerOcupacion(
                laboratorioId,
                fecha
        );
    }
}