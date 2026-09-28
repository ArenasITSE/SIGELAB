package sigelab_api.carrera.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sigelab_api.carrera.dto.CarreraRequest;
import sigelab_api.carrera.dto.CarreraResponse;
import sigelab_api.carrera.entity.Carrera;
import sigelab_api.carrera.repository.CarreraRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarreraServiceImpl
        implements CarreraService {

    private final CarreraRepository carreraRepository;

    @Override
    public CarreraResponse crear(
            CarreraRequest request
    ) {

        Carrera carrera = Carrera.builder()
                .clave(request.clave())
                .nombre(request.nombre())
                .activo(true)
                .build();

        carrera = carreraRepository.save(carrera);

        return mapToResponse(carrera);
    }

    @Override
    public List<CarreraResponse> listar() {

        return carreraRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public CarreraResponse buscarPorId(Long id) {

        Carrera carrera =
                carreraRepository.findById(id)
                        .orElseThrow();

        return mapToResponse(carrera);
    }

    @Override
    public CarreraResponse actualizar(
            Long id,
            CarreraRequest request
    ) {

        Carrera carrera =
                carreraRepository.findById(id)
                        .orElseThrow();

        carrera.setClave(request.clave());
        carrera.setNombre(request.nombre());

        carrera = carreraRepository.save(carrera);

        return mapToResponse(carrera);
    }

    @Override
    public void eliminar(Long id) {

        Carrera carrera =
                carreraRepository.findById(id)
                        .orElseThrow();

        carrera.setActivo(false);

        carreraRepository.save(carrera);
    }

    private CarreraResponse mapToResponse(
            Carrera carrera
    ) {

        return new CarreraResponse(
                carrera.getId(),
                carrera.getClave(),
                carrera.getNombre(),
                carrera.getActivo()
        );
    }
}