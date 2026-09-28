package sigelab_api.materia.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import sigelab_api.carrera.entity.Carrera;
import sigelab_api.carrera.repository.CarreraRepository;
import sigelab_api.materia.dto.MateriaRequest;
import sigelab_api.materia.dto.MateriaResponse;
import sigelab_api.materia.entity.Materia;
import sigelab_api.materia.repository.MateriaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MateriaServiceImpl
        implements MateriaService {

    private final MateriaRepository materiaRepository;
    private final CarreraRepository carreraRepository;

    @Override
    public MateriaResponse crear(
            MateriaRequest request
    ) {

        Carrera carrera = carreraRepository
                .findById(request.carreraId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Carrera no encontrada"
                        )
                );

        Materia materia = Materia.builder()
                .clave(request.clave())
                .nombre(request.nombre())
                .semestre(request.semestre())
                .horasSemana(request.horasSemana())
                .activo(true)
                .carrera(carrera)
                .build();

        materia = materiaRepository.save(materia);

        return mapToResponse(materia);
    }

    @Override
    public List<MateriaResponse> listar() {

        return materiaRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public MateriaResponse buscarPorId(
            Long id
    ) {

        Materia materia = materiaRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Materia no encontrada"
                        )
                );

        return mapToResponse(materia);
    }

    @Override
    public MateriaResponse actualizar(
            Long id,
            MateriaRequest request
    ) {

        Materia materia = materiaRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Materia no encontrada"
                        )
                );

        Carrera carrera = carreraRepository
                .findById(request.carreraId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Carrera no encontrada"
                        )
                );

        materia.setClave(request.clave());
        materia.setNombre(request.nombre());
        materia.setSemestre(request.semestre());
        materia.setHorasSemana(request.horasSemana());
        materia.setCarrera(carrera);

        materia = materiaRepository.save(materia);

        return mapToResponse(materia);
    }

    @Override
    public void eliminar(
            Long id
    ) {

        Materia materia = materiaRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Materia no encontrada"
                        )
                );

        materia.setActivo(false);

        materiaRepository.save(materia);
    }

    private MateriaResponse mapToResponse(
            Materia materia
    ) {

        return new MateriaResponse(
                materia.getId(),
                materia.getClave(),
                materia.getNombre(),
                materia.getSemestre(),
                materia.getHorasSemana(),
                materia.getCarrera().getId(),
                materia.getCarrera().getNombre(),
                materia.getActivo()
        );
    }
}