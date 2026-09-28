package sigelab_api.calendario.service;

import sigelab_api.calendario.dto.OcupacionLaboratorioResponse;

import java.time.LocalDate;
import java.util.List;

public interface CalendarioService {

    List<OcupacionLaboratorioResponse> obtenerOcupacion(
            Long laboratorioId,
            LocalDate fecha
    );
}