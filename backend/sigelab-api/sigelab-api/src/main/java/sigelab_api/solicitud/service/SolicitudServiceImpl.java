
package sigelab_api.solicitud.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sigelab_api.laboratorio.entity.Laboratorio;
import sigelab_api.laboratorio.repository.LaboratorioRepository;

import sigelab_api.materia.entity.Materia;
import sigelab_api.materia.repository.MateriaRepository;

import sigelab_api.usuario.entity.Usuario;

import sigelab_api.solicitud.dto.SolicitudRequest;
import sigelab_api.solicitud.dto.SolicitudResponse;
import sigelab_api.solicitud.entity.EstadoSolicitud;
import sigelab_api.solicitud.entity.SolicitudLaboratorio;
import sigelab_api.solicitud.repository.SolicitudLaboratorioRepository;

import sigelab_api.usuario.service.UsuarioAutenticado;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudLaboratorioRepository solicitudRepository;
    private final MateriaRepository materiaRepository;
    private final LaboratorioRepository laboratorioRepository;
    private final UsuarioAutenticado usuarioAutenticado;

    // ==========================================
    // CREAR SOLICITUD
    // ==========================================

    @Override
    public SolicitudResponse crear(SolicitudRequest request) {

        Usuario usuario = usuarioAutenticado.obtenerUsuario();

        String rol = usuario.getRol().name();

        if (!rol.equals("DOCENTE")
                && !rol.equals("COORDINADOR")
                && !rol.equals("ADMINISTRADOR")) {

            throw new AccessDeniedException(
                    "No tienes permisos para crear solicitudes."
            );
        }

        validarHorario(request);

        if (request.fecha().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "No puedes solicitar un laboratorio para una fecha pasada."
            );
        }

        // Obtener materia.
        Materia materia = materiaRepository.findById(request.materiaId())
                .orElseThrow(() ->
                        new RuntimeException("Materia no encontrada.")
                );

        // Obtener laboratorio.
        Laboratorio laboratorio = laboratorioRepository
                .findById(request.laboratorioId())
                .orElseThrow(() ->
                        new RuntimeException("Laboratorio no encontrado.")
                );

        // ==========================================
        // VALIDAR CARRERAS
        // ==========================================

        if (materia.getCarrera() == null
                || materia.getCarrera().getId() == null) {

            throw new IllegalArgumentException(
                    "La materia no tiene una carrera asignada."
            );
        }

        if (laboratorio.getCarrera() == null
                || laboratorio.getCarrera().getId() == null) {

            throw new IllegalArgumentException(
                    "El laboratorio no tiene una carrera asignada."
            );
        }

        Long carreraMateriaId = materia.getCarrera().getId();

        Long carreraLaboratorioId = laboratorio.getCarrera().getId();

        // La materia y el laboratorio siempre deben pertenecer
        // a la misma carrera, independientemente del rol.

        if (!Objects.equals(carreraMateriaId, carreraLaboratorioId)) {

            throw new IllegalArgumentException(
                    "La materia y el laboratorio deben pertenecer a la misma carrera."
            );
        }

        // ==========================================
        // VALIDAR PERMISOS SEGÚN ROL
        // ==========================================

        switch (rol) {

            case "DOCENTE":

                // El docente puede utilizar cualquiera de sus
                // carreras asignadas.

                if (!perteneceACarrera(usuario, carreraMateriaId)) {

                    throw new AccessDeniedException(
                            "No puedes solicitar laboratorios de una carrera que no tienes asignada."
                    );
                }

                break;

            case "COORDINADOR":

                // El coordinador conserva acceso únicamente
                // a su carrera principal.

                Long carreraCoordinadorId =
                        usuarioAutenticado.obtenerCarreraId();

                if (!Objects.equals(
                        carreraLaboratorioId,
                        carreraCoordinadorId
                )) {

                    throw new AccessDeniedException(
                            "No puedes solicitar un laboratorio de otra carrera."
                    );
                }

                break;

            case "ADMINISTRADOR":

                // El administrador conserva acceso global.

                break;

            default:

                throw new AccessDeniedException(
                        "No tienes permisos para crear solicitudes."
                );
        }

        // ==========================================
        // VALIDAR DISPONIBILIDAD DEL LABORATORIO
        // ==========================================

        if (laboratorio.getEstatus()
                != sigelab_api.laboratorio.entity.EstatusLaboratorio.DISPONIBLE) {

            throw new IllegalArgumentException(
                    "El laboratorio no se encuentra disponible."
            );
        }

        // ==========================================
        // VALIDAR CONFLICTO DE HORARIO
        // ==========================================

        boolean conflicto =
                solicitudRepository.existsConflictoHorarioCrear(
                        laboratorio.getId(),
                        request.fecha(),
                        request.horaInicio(),
                        request.horaFin()
                );

        if (conflicto) {
            throw new IllegalArgumentException(
                    "El laboratorio ya tiene una reservación aprobada en ese horario."
            );
        }

        // ==========================================
        // REGISTRAR SOLICITUD
        // ==========================================

        SolicitudLaboratorio solicitud = SolicitudLaboratorio.builder()
                .docente(usuario)
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

    // ==========================================
    // VALIDAR SI EL USUARIO PERTENECE A CARRERA
    // ==========================================

    private boolean perteneceACarrera(
            Usuario usuario,
            Long carreraId
    ) {

        if (usuario == null || carreraId == null) {
            return false;
        }

        // Revisar las carreras asignadas mediante ManyToMany.
        if (usuario.getCarreras() != null
                && usuario.getCarreras().stream()
                .filter(Objects::nonNull)
                .anyMatch(carrera ->
                        Objects.equals(carrera.getId(), carreraId)
                )) {

            return true;
        }

        // Compatibilidad con usuarios que solamente
        // tienen carrera principal asignada.
        return usuario.getCarrera() != null
                && Objects.equals(
                        usuario.getCarrera().getId(),
                        carreraId
                );
    }

    // ==========================================
    // LISTAR SOLICITUDES SEGÚN ROL
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudResponse> listar() {

        Usuario usuario = usuarioAutenticado.obtenerUsuario();

        String rol = usuario.getRol().name();

        List<SolicitudLaboratorio> solicitudes;

        if (rol.equals("ADMINISTRADOR")) {

            solicitudes = solicitudRepository.findAll();

        } else if (rol.equals("COORDINADOR")) {

            Long carreraId = usuarioAutenticado.obtenerCarreraId();

            solicitudes = solicitudRepository
                    .findAllByLaboratorio_Carrera_Id(carreraId);

        } else if (rol.equals("DOCENTE")) {

            solicitudes = solicitudRepository
                    .findByDocenteId(usuario.getId());

        } else {

            throw new AccessDeniedException(
                    "No tienes permisos para consultar solicitudes."
            );
        }

        return solicitudes.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ==========================================
    // BUSCAR SOLICITUD POR ID
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public SolicitudResponse buscarPorId(Long id) {

        SolicitudLaboratorio solicitud =
                obtenerSolicitudAutorizada(id);

        return mapToResponse(solicitud);
    }

    // ==========================================
    // APROBAR SOLICITUD
    // ==========================================

    @Override
    public SolicitudResponse aprobar(
            Long id,
            String observaciones
    ) {

        validarPermisoGestion();

        SolicitudLaboratorio solicitud =
                obtenerSolicitudAutorizada(id);

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new IllegalArgumentException(
                    "Solamente se pueden aprobar solicitudes pendientes."
            );
        }

        boolean conflicto =
                solicitudRepository.existsConflictoHorario(
                        solicitud.getLaboratorio().getId(),
                        solicitud.getFecha(),
                        solicitud.getHoraInicio(),
                        solicitud.getHoraFin(),
                        solicitud.getId()
                );

        if (conflicto) {
            throw new IllegalArgumentException(
                    "El laboratorio ya se encuentra reservado en ese horario."
            );
        }

        solicitud.setEstado(EstadoSolicitud.APROBADA);
        solicitud.setObservaciones(observaciones);

        solicitud = solicitudRepository.save(solicitud);

        return mapToResponse(solicitud);
    }

    // ==========================================
    // RECHAZAR SOLICITUD
    // ==========================================

    @Override
    public SolicitudResponse rechazar(
            Long id,
            String observaciones
    ) {

        validarPermisoGestion();

        SolicitudLaboratorio solicitud =
                obtenerSolicitudAutorizada(id);

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new IllegalArgumentException(
                    "Solamente se pueden rechazar solicitudes pendientes."
            );
        }

        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setObservaciones(observaciones);

        solicitud = solicitudRepository.save(solicitud);

        return mapToResponse(solicitud);
    }

    // ==========================================
    // ELIMINAR SOLICITUD
    // ==========================================

    @Override
    public void eliminar(Long id) {

        if (!usuarioAutenticado.esAdministrador()) {
            throw new AccessDeniedException(
                    "Solo un administrador puede eliminar solicitudes."
            );
        }

        SolicitudLaboratorio solicitud =
                solicitudRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Solicitud no encontrada."
                                )
                        );

        solicitudRepository.delete(solicitud);
    }

    // ==========================================
    // LISTAR MIS SOLICITUDES
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudResponse> listarMisSolicitudes() {

        Usuario usuario = usuarioAutenticado.obtenerUsuario();

        if (!usuario.getRol().name().equals("DOCENTE")) {
            throw new AccessDeniedException(
                    "Esta operación está disponible para docentes."
            );
        }

        return solicitudRepository
                .findByDocenteId(usuario.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ==========================================
    // VALIDAR PERMISOS DE APROBACIÓN Y RECHAZO
    // ==========================================

    private void validarPermisoGestion() {

        Usuario usuario = usuarioAutenticado.obtenerUsuario();

        String rol = usuario.getRol().name();

        if (!rol.equals("ADMINISTRADOR")
                && !rol.equals("COORDINADOR")) {

            throw new AccessDeniedException(
                    "No tienes permisos para gestionar solicitudes."
            );
        }
    }

    // ==========================================
    // OBTENER SOLICITUD SEGÚN PERMISOS
    // ==========================================

    private SolicitudLaboratorio obtenerSolicitudAutorizada(Long id) {

        Usuario usuario = usuarioAutenticado.obtenerUsuario();

        String rol = usuario.getRol().name();

        if (rol.equals("ADMINISTRADOR")) {

            return solicitudRepository.findById(id)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Solicitud no encontrada."
                            )
                    );
        }

        if (rol.equals("COORDINADOR")) {

            Long carreraId = usuarioAutenticado.obtenerCarreraId();

            return solicitudRepository
                    .findByIdAndLaboratorio_Carrera_Id(id, carreraId)
                    .orElseThrow(() ->
                            new AccessDeniedException(
                                    "La solicitud no existe o no pertenece a tu carrera."
                            )
                    );
        }

        if (rol.equals("DOCENTE")) {

            return solicitudRepository
                    .findByIdAndDocenteId(id, usuario.getId())
                    .orElseThrow(() ->
                            new AccessDeniedException(
                                    "La solicitud no existe o no te pertenece."
                            )
                    );
        }

        throw new AccessDeniedException(
                "No tienes permisos para consultar esta solicitud."
        );
    }

    // ==========================================
    // VALIDAR HORARIO
    // ==========================================

    private void validarHorario(SolicitudRequest request) {

        if (request.horaInicio() == null
                || request.horaFin() == null) {

            throw new IllegalArgumentException(
                    "Debes indicar la hora de inicio y finalización."
            );
        }

        if (!request.horaFin().isAfter(request.horaInicio())) {
            throw new IllegalArgumentException(
                    "La hora de finalización debe ser posterior a la hora de inicio."
            );
        }
    }

    // ==========================================
    // MAPEAR ENTIDAD A RESPONSE
    // ==========================================

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

}
