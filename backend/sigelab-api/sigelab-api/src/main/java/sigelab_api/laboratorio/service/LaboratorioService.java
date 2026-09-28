package sigelab_api.laboratorio.service;

import sigelab_api.laboratorio.dto.LaboratorioRequest;
import sigelab_api.laboratorio.dto.LaboratorioResponse;

import java.util.List;

public interface LaboratorioService {

    LaboratorioResponse crear(
            LaboratorioRequest request
    );

    List<LaboratorioResponse> listar();

    LaboratorioResponse buscarPorId(
            Long id
    );

    LaboratorioResponse actualizar(
            Long id,
            LaboratorioRequest request
    );

    void eliminar(
            Long id
    );
}