package sigelab_api.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import sigelab_api.dashboard.dto.DashboardResponse;

import sigelab_api.usuario.entity.Rol;
import sigelab_api.usuario.repository.UsuarioRepository;

import sigelab_api.laboratorio.repository.LaboratorioRepository;
import sigelab_api.materia.repository.MateriaRepository;

import sigelab_api.solicitud.entity.EstadoSolicitud;
import sigelab_api.solicitud.repository.SolicitudLaboratorioRepository;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl
        implements DashboardService {

    private final UsuarioRepository usuarioRepository;

    private final LaboratorioRepository laboratorioRepository;

    private final MateriaRepository materiaRepository;

    private final SolicitudLaboratorioRepository solicitudRepository;

    @Override
    public DashboardResponse obtenerResumen() {

        return new DashboardResponse(

                usuarioRepository.count(),

                usuarioRepository.countByRol(
                        Rol.DOCENTE
                ),

                usuarioRepository.countByRol(
                        Rol.COORDINADOR
                ),

                usuarioRepository.countByRol(
                        Rol.ADMINISTRADOR
                ),

                laboratorioRepository.count(),

                materiaRepository.count(),

                solicitudRepository.countByEstado(
                        EstadoSolicitud.PENDIENTE
                ),

                solicitudRepository.countByEstado(
                        EstadoSolicitud.APROBADA
                ),

                solicitudRepository.countByEstado(
                        EstadoSolicitud.RECHAZADA
                )
        );
    }
}