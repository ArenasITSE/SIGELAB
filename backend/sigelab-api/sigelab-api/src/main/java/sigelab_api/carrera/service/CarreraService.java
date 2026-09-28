package sigelab_api.carrera.service;

import sigelab_api.carrera.dto.CarreraRequest;
import sigelab_api.carrera.dto.CarreraResponse;

import java.util.List;

public interface CarreraService {

    CarreraResponse crear(CarreraRequest request);

    List<CarreraResponse> listar();

    CarreraResponse buscarPorId(Long id);

    CarreraResponse actualizar(
            Long id,
            CarreraRequest request
    );

    void eliminar(Long id);
}