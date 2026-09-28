package sigelab_api.bitacora.service;

import sigelab_api.bitacora.dto.BitacoraRequest;
import sigelab_api.bitacora.dto.BitacoraResponse;

import java.util.List;

public interface BitacoraService {

    BitacoraResponse crear(
            BitacoraRequest request
    );

    List<BitacoraResponse> listar();

    BitacoraResponse buscarPorId(
            Long id
    );

    void eliminar(
            Long id
    );
}