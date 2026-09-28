package sigelab_api.solicitud.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import sigelab_api.laboratorio.entity.Laboratorio;
import sigelab_api.laboratorio.repository.LaboratorioRepository;
import sigelab_api.materia.entity.Materia;
import sigelab_api.materia.repository.MateriaRepository;
import sigelab_api.usuario.entity.Usuario;
import sigelab_api.usuario.repository.UsuarioRepository;
import sigelab_api.solicitud.dto.SolicitudRequest;
import sigelab_api.solicitud.dto.SolicitudResponse;
import sigelab_api.solicitud.entity.EstadoSolicitud;
import sigelab_api.solicitud.entity.SolicitudLaboratorio;
import sigelab_api.solicitud.repository.SolicitudLaboratorioRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudLaboratorioRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final MateriaRepository materiaRepository;
    private final LaboratorioRepository laboratorioRepository;

   @Override
public SolicitudResponse crear(
        SolicitudRequest request,
        String correoUsuario
) {

    Usuario docente = usuarioRepository
            .findByCorreo(correoUsuario)
            .orElseThrow(() ->
                    new RuntimeException("Usuario no encontrado"));

    Materia materia = materiaRepository
            .findById(request.materiaId())
            .orElseThrow(() ->
                    new RuntimeException("Materia no encontrada"));

    Laboratorio laboratorio = laboratorioRepository
            .findById(request.laboratorioId())
            .orElseThrow(() ->
                    new RuntimeException("Laboratorio no encontrado"));

    boolean conflicto =
            solicitudRepository.existsConflictoHorarioCrear(
                    laboratorio.getId(),
                    request.fecha(),
                    request.horaInicio(),
                    request.horaFin()
            );

    if (conflicto) {
        throw new RuntimeException(
                "El laboratorio ya tiene una reservación aprobada en ese horario"
        );
    }

    SolicitudLaboratorio solicitud =
            SolicitudLaboratorio.builder()
                    .docente(docente)
                    .materia(materia)
                    .laboratorio(laboratorio)
                    .fecha(request.fecha())
                    .horaInicio(request.horaInicio())
                    .horaFin(request.horaFin())
                    .motivo(request.motivo())
                    .estado(EstadoSolicitud.PENDIENTE)
                    .build();

    solicitud = solicitudRepository.save(solicitud);

    return mapToResponse(solicitud);
}
    @Override
    public List<SolicitudResponse> listar() {
        return solicitudRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public SolicitudResponse buscarPorId(Long id) {

        SolicitudLaboratorio solicitud =
                solicitudRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Solicitud no encontrada"));

        return mapToResponse(solicitud);
    }

    @Override
    public SolicitudResponse aprobar(
            Long id,
            String observaciones
    ) {

        SolicitudLaboratorio solicitud =
                solicitudRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Solicitud no encontrada"));

        boolean conflicto =
                solicitudRepository.existsConflictoHorario(
                        solicitud.getLaboratorio().getId(),
                        solicitud.getFecha(),
                        solicitud.getHoraInicio(),
                        solicitud.getHoraFin(),
                        solicitud.getId()
                );

        if (conflicto) {
            throw new RuntimeException(
                    "El laboratorio ya se encuentra reservado en ese horario"
            );
        }

        solicitud.setEstado(EstadoSolicitud.APROBADA);
        solicitud.setObservaciones(observaciones);

        solicitud = solicitudRepository.save(solicitud);

        return mapToResponse(solicitud);
    }

    @Override
    public SolicitudResponse rechazar(
            Long id,
            String observaciones
    ) {

        SolicitudLaboratorio solicitud =
                solicitudRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Solicitud no encontrada"));

        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setObservaciones(observaciones);

        solicitud = solicitudRepository.save(solicitud);

        return mapToResponse(solicitud);
    }

    @Override
    public void eliminar(Long id) {

        SolicitudLaboratorio solicitud =
                solicitudRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Solicitud no encontrada"));

        solicitudRepository.delete(solicitud);
    }

    private SolicitudResponse mapToResponse(
            SolicitudLaboratorio solicitud
    ) {

        return new SolicitudResponse(
                solicitud.getId(),
                solicitud.getDocente().getId(),
                solicitud.getDocente().getNombre(),
                solicitud.getMateria().getId(),
                solicitud.getMateria().getNombre(),
                solicitud.getLaboratorio().getId(),
                solicitud.getLaboratorio().getNombre(),
                solicitud.getFecha(),
                solicitud.getHoraInicio(),
                solicitud.getHoraFin(),
                solicitud.getMotivo(),
                solicitud.getEstado().name(),
                solicitud.getObservaciones()
        );
    

}
@Override
public List<SolicitudResponse> listarMisSolicitudes(Long docenteId) {

    return solicitudRepository
            .findByDocenteId(docenteId)
            .stream()
            .map(this::mapToResponse)
            .toList();
}
    
}