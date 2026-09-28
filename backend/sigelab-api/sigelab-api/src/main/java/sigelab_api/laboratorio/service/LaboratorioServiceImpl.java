package sigelab_api.laboratorio.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import sigelab_api.carrera.entity.Carrera;
import sigelab_api.carrera.repository.CarreraRepository;

import sigelab_api.laboratorio.dto.LaboratorioRequest;
import sigelab_api.laboratorio.dto.LaboratorioResponse;
import sigelab_api.laboratorio.entity.EstatusLaboratorio;
import sigelab_api.laboratorio.entity.Laboratorio;
import sigelab_api.laboratorio.repository.LaboratorioRepository;

import sigelab_api.usuario.entity.Usuario;
import sigelab_api.usuario.repository.UsuarioRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LaboratorioServiceImpl
        implements LaboratorioService {

    private final LaboratorioRepository laboratorioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarreraRepository carreraRepository;

    @Override
    public LaboratorioResponse crear(
            LaboratorioRequest request
    ) {

        Usuario encargado =
                usuarioRepository.findById(
                        request.encargadoId()
                ).orElseThrow(
                        () -> new RuntimeException(
                                "Encargado no encontrado"
                        )
                );

        Carrera carrera =
                carreraRepository.findById(
                        request.carreraId()
                ).orElseThrow(
                        () -> new RuntimeException(
                                "Carrera no encontrada"
                        )
                );

        Laboratorio laboratorio =
                Laboratorio.builder()
                        .nombre(request.nombre())
                        .encargado(encargado)
                        .carrera(carrera)
                        .ubicacion(request.ubicacion())
                        .capacidad(request.capacidad())
                        .estatus(EstatusLaboratorio.DISPONIBLE)
                        .build();

        laboratorio =
                laboratorioRepository.save(laboratorio);

        return mapToResponse(laboratorio);
    }

    @Override
    public List<LaboratorioResponse> listar() {

        return laboratorioRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public LaboratorioResponse buscarPorId(
            Long id
    ) {

        Laboratorio laboratorio =
                laboratorioRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Laboratorio no encontrado"
                                )
                        );

        return mapToResponse(laboratorio);
    }

    @Override
    public LaboratorioResponse actualizar(
            Long id,
            LaboratorioRequest request
    ) {

        Laboratorio laboratorio =
                laboratorioRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Laboratorio no encontrado"
                                )
                        );

        Usuario encargado =
                usuarioRepository.findById(
                        request.encargadoId()
                ).orElseThrow(
                        () -> new RuntimeException(
                                "Encargado no encontrado"
                        )
                );

        Carrera carrera =
                carreraRepository.findById(
                        request.carreraId()
                ).orElseThrow(
                        () -> new RuntimeException(
                                "Carrera no encontrada"
                        )
                );

        laboratorio.setNombre(request.nombre());
        laboratorio.setEncargado(encargado);
        laboratorio.setCarrera(carrera);
        laboratorio.setUbicacion(request.ubicacion());
        laboratorio.setCapacidad(request.capacidad());

        laboratorio =
                laboratorioRepository.save(laboratorio);

        return mapToResponse(laboratorio);
    }

    @Override
    public void eliminar(
            Long id
    ) {

        Laboratorio laboratorio =
                laboratorioRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Laboratorio no encontrado"
                                )
                        );

        laboratorio.setEstatus(
                EstatusLaboratorio.INACTIVO
        );

        laboratorioRepository.save(laboratorio);
    }

    private LaboratorioResponse mapToResponse(
            Laboratorio laboratorio
    ) {

        return new LaboratorioResponse(
                laboratorio.getId(),
                laboratorio.getNombre(),

                laboratorio.getEncargado().getId(),
                laboratorio.getEncargado().getNombre(),

                laboratorio.getCarrera().getId(),
                laboratorio.getCarrera().getNombre(),

                laboratorio.getUbicacion(),
                laboratorio.getCapacidad(),
                laboratorio.getEstatus().name()
        );
    }
}