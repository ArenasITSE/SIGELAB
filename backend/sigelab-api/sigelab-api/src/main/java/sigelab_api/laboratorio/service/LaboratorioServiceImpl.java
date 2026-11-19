
package sigelab_api.laboratorio.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sigelab_api.carrera.entity.Carrera;
import sigelab_api.carrera.repository.CarreraRepository;

import sigelab_api.laboratorio.dto.LaboratorioRequest;
import sigelab_api.laboratorio.dto.LaboratorioResponse;
import sigelab_api.laboratorio.entity.EstatusLaboratorio;
import sigelab_api.laboratorio.entity.Laboratorio;
import sigelab_api.laboratorio.repository.LaboratorioRepository;
import sigelab_api.usuario.service.UsuarioAutenticado;

import sigelab_api.usuario.entity.Usuario;
import sigelab_api.usuario.repository.UsuarioRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LaboratorioServiceImpl implements LaboratorioService {

    private final LaboratorioRepository laboratorioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarreraRepository carreraRepository;
    private final UsuarioAutenticado usuarioAutenticado;

    // ==========================================
    // REGISTRAR LABORATORIO
    // ==========================================

    @Override
    public LaboratorioResponse crear(LaboratorioRequest request) {

        Usuario usuarioActual = usuarioAutenticado.obtenerUsuario();

        Carrera carrera;

        if (usuarioActual.getRol().name().equals("ADMINISTRADOR")) {

            if (request.carreraId() == null) {
                throw new IllegalArgumentException(
                        "Debes seleccionar una carrera."
                );
            }

            carrera = carreraRepository.findById(request.carreraId())
                    .orElseThrow(() ->
                            new RuntimeException("Carrera no encontrada")
                    );

        } else {

            carrera = usuarioActual.getCarrera();

            if (carrera == null) {
                throw new AccessDeniedException(
                        "No tienes una carrera asignada."
                );
            }
        }

        Usuario encargado = usuarioRepository.findById(
                request.encargadoId()
        ).orElseThrow(() ->
                new RuntimeException("Encargado no encontrado")
        );

        // El coordinador solamente puede asignar encargados
        // pertenecientes a su propia carrera.
        if (!usuarioActual.getRol().name().equals("ADMINISTRADOR")) {

            if (encargado.getCarrera() == null
                    || !encargado.getCarrera().getId()
                            .equals(carrera.getId())) {

                throw new AccessDeniedException(
                        "No puedes asignar un encargado de otra carrera."
                );
            }
        }

        Laboratorio laboratorio = Laboratorio.builder()
                .nombre(request.nombre())
                .encargado(encargado)
                .carrera(carrera)
                .ubicacion(request.ubicacion())
                .capacidad(request.capacidad())
                .estatus(EstatusLaboratorio.DISPONIBLE)
                .build();

        laboratorio = laboratorioRepository.save(laboratorio);

        return mapToResponse(laboratorio);
    }

    // ==========================================
    // LISTAR LABORATORIOS
    // ==========================================

@Override
@Transactional(readOnly = true)
public List<LaboratorioResponse> listar() {

    Usuario usuario = usuarioAutenticado.obtenerUsuario();

    List<Laboratorio> laboratorios;

    // El administrador consulta todos los laboratorios.
    if (usuario.getRol().name().equals("ADMINISTRADOR")) {

        laboratorios = laboratorioRepository.findAll();

    } else {

        // Coordinadores y docentes necesitan tener una carrera asignada.
        Long carreraId = usuarioAutenticado.obtenerCarreraId();

        laboratorios = laboratorioRepository
                .findAllByCarrera_Id(carreraId);

        // El docente solamente puede consultar laboratorios disponibles.
        if (usuario.getRol().name().equals("DOCENTE")) {

            laboratorios = laboratorios.stream()
                    .filter(lab -> lab.getEstatus() == EstatusLaboratorio.DISPONIBLE)
                    .toList();
        }
    }

    return laboratorios.stream()
            .map(this::mapToResponse)
            .toList();
}
    // ==========================================
    // BUSCAR LABORATORIO POR ID
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public LaboratorioResponse buscarPorId(Long id) {

        Laboratorio laboratorio = obtenerLaboratorioAutorizado(id);

        return mapToResponse(laboratorio);
    }

    // ==========================================
    // ACTUALIZAR LABORATORIO
    // ==========================================

    @Override
    public LaboratorioResponse actualizar(
            Long id,
            LaboratorioRequest request
    ) {

        Usuario usuarioActual = usuarioAutenticado.obtenerUsuario();

        Laboratorio laboratorio = obtenerLaboratorioAutorizado(id);

        if (laboratorio.getEstatus() == EstatusLaboratorio.INACTIVO) {
            throw new RuntimeException(
                    "No se puede actualizar un laboratorio inactivo."
            );
        }

        Usuario encargado = usuarioRepository.findById(
                request.encargadoId()
        ).orElseThrow(() ->
                new RuntimeException("Encargado no encontrado")
        );

        if (!usuarioActual.getRol().name().equals("ADMINISTRADOR")) {

            Long carreraId = usuarioAutenticado.obtenerCarreraId();

            if (encargado.getCarrera() == null
                    || !encargado.getCarrera().getId().equals(carreraId)) {

                throw new AccessDeniedException(
                        "No puedes asignar un encargado de otra carrera."
                );
            }

            // No se modifica la carrera del laboratorio.
        } else {

            if (request.carreraId() == null) {
                throw new IllegalArgumentException(
                        "Debes seleccionar una carrera."
                );
            }

            Carrera carrera = carreraRepository.findById(
                    request.carreraId()
            ).orElseThrow(() ->
                    new RuntimeException("Carrera no encontrada")
            );

            laboratorio.setCarrera(carrera);
        }

        laboratorio.setNombre(request.nombre());
        laboratorio.setEncargado(encargado);
        laboratorio.setUbicacion(request.ubicacion());
        laboratorio.setCapacidad(request.capacidad());

        laboratorio = laboratorioRepository.save(laboratorio);

        return mapToResponse(laboratorio);
    }

    // ==========================================
    // DESACTIVAR LABORATORIO
    // ==========================================

    @Override
    public void eliminar(Long id) {

        Laboratorio laboratorio = obtenerLaboratorioAutorizado(id);

        if (laboratorio.getEstatus() == EstatusLaboratorio.INACTIVO) {
            throw new RuntimeException(
                    "El laboratorio ya se encuentra inactivo."
            );
        }

        laboratorio.setEstatus(EstatusLaboratorio.INACTIVO);

        laboratorioRepository.save(laboratorio);
    }

    // ==========================================
    // ELIMINAR DEFINITIVAMENTE
    // ==========================================

    @Override
    public void eliminarDefinitivamente(Long id) {

        if (!usuarioAutenticado.esAdministrador()) {
            throw new AccessDeniedException(
                    "Solo un administrador puede eliminar definitivamente."
            );
        }

        Laboratorio laboratorio = laboratorioRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Laboratorio no encontrado")
                );

        if (laboratorio.getEstatus() != EstatusLaboratorio.INACTIVO) {
            throw new RuntimeException(
                    "Primero debes desactivar el laboratorio."
            );
        }

        laboratorioRepository.delete(laboratorio);
    }

    // ==========================================
    // OBTENER LABORATORIO SEGÚN PERMISOS
    // ==========================================

    private Laboratorio obtenerLaboratorioAutorizado(Long id) {

        Usuario usuario = usuarioAutenticado.obtenerUsuario();

        if (usuario.getRol().name().equals("ADMINISTRADOR")) {

            return laboratorioRepository.findById(id)
                    .orElseThrow(() ->
                            new RuntimeException("Laboratorio no encontrado")
                    );
        }

        Long carreraId = usuarioAutenticado.obtenerCarreraId();

        return laboratorioRepository
                .findByIdAndCarrera_Id(id, carreraId)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "El laboratorio no existe o no pertenece a tu carrera."
                        )
                );
    }

    // ==========================================
    // MAPEAR ENTIDAD A RESPONSE
    // ==========================================

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
