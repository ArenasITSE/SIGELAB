
package sigelab_api.materia.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sigelab_api.carrera.entity.Carrera;
import sigelab_api.carrera.repository.CarreraRepository;

import sigelab_api.materia.dto.MateriaRequest;
import sigelab_api.materia.dto.MateriaResponse;
import sigelab_api.materia.entity.Materia;
import sigelab_api.materia.repository.MateriaRepository;

import sigelab_api.usuario.entity.Usuario;
import sigelab_api.usuario.service.UsuarioAutenticado;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MateriaServiceImpl implements MateriaService {

    private final MateriaRepository materiaRepository;
    private final CarreraRepository carreraRepository;
    private final UsuarioAutenticado usuarioAutenticado;

    // ==========================================
    // CREAR MATERIA
    // ==========================================

    @Override
    public MateriaResponse crear(MateriaRequest request) {

        Usuario usuario = usuarioAutenticado.obtenerUsuario();

        String rol = usuario.getRol().name();

        Carrera carrera;

        if ("ADMINISTRADOR".equals(rol)) {

            // El administrador puede elegir cualquier carrera.
            carrera = buscarCarrera(request.carreraId());

        } else if ("COORDINADOR".equals(rol)) {

            // El coordinador utiliza exclusivamente su carrera.
            carrera = obtenerCarreraDelUsuario(usuario);

        } else {

            throw new AccessDeniedException(
                    "No tienes permisos para registrar materias."
            );
        }

        validarClaveDuplicada(
                request.clave(),
                carrera.getId(),
                null
        );

        Materia materia = Materia.builder()
                .clave(request.clave().trim())
                .nombre(request.nombre().trim())
                .semestre(request.semestre())
                .horasSemana(request.horasSemana())
                .activo(true)
                .carrera(carrera)
                .build();

        materia = materiaRepository.save(materia);

        return mapToResponse(materia);
    }

    // ==========================================
    // LISTAR MATERIAS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<MateriaResponse> listar() {

        Usuario usuario = usuarioAutenticado.obtenerUsuario();

        String rol = usuario.getRol().name();

        List<Materia> materias;

        if ("ADMINISTRADOR".equals(rol)) {

            // El administrador consulta todas las materias.
            materias = materiaRepository.findAll();

        } else if ("COORDINADOR".equals(rol)) {

            // El coordinador consulta todas las materias de su carrera.
            Long carreraId = obtenerCarreraDelUsuario(usuario).getId();

            materias = materiaRepository.findByCarreraId(carreraId);

        } else if ("DOCENTE".equals(rol)) {

            // El docente consulta únicamente materias activas
            // de su carrera.
            Long carreraId = obtenerCarreraDelUsuario(usuario).getId();

            materias = materiaRepository
                    .findByCarreraIdAndActivoTrue(carreraId);

        } else {

            throw new AccessDeniedException(
                    "No tienes permisos para consultar materias."
            );
        }

        return materias.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ==========================================
    // BUSCAR MATERIA POR ID
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public MateriaResponse buscarPorId(Long id) {

        Usuario usuario = usuarioAutenticado.obtenerUsuario();

        String rol = usuario.getRol().name();

        Materia materia = materiaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Materia no encontrada."
                ));

        if ("ADMINISTRADOR".equals(rol)) {

            return mapToResponse(materia);
        }

        if ("COORDINADOR".equals(rol)) {

            validarCarreraDelCoordinador(
                    materia,
                    usuario
            );

            return mapToResponse(materia);
        }

        if ("DOCENTE".equals(rol)) {

            validarCarreraDelCoordinador(
                    materia,
                    usuario
            );

            if (!Boolean.TRUE.equals(materia.getActivo())) {
                throw new IllegalArgumentException(
                        "La materia no se encuentra disponible."
                );
            }

            return mapToResponse(materia);
        }

        throw new AccessDeniedException(
                "No tienes permisos para consultar esta materia."
        );
    }

    // ==========================================
    // ACTUALIZAR MATERIA
    // ==========================================

    @Override
    public MateriaResponse actualizar(
            Long id,
            MateriaRequest request
    ) {

        Usuario usuario = usuarioAutenticado.obtenerUsuario();

        String rol = usuario.getRol().name();

        Materia materia = materiaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Materia no encontrada."
                ));

        Carrera carrera;

        if ("ADMINISTRADOR".equals(rol)) {

            // El administrador puede cambiar la materia
            // a cualquier carrera.
            carrera = buscarCarrera(request.carreraId());

        } else if ("COORDINADOR".equals(rol)) {

            // Comprueba que la materia pertenezca
            // a la carrera del coordinador.
            validarCarreraDelCoordinador(
                    materia,
                    usuario
            );

            // El coordinador no puede cambiar la carrera.
            carrera = obtenerCarreraDelUsuario(usuario);

        } else {

            throw new AccessDeniedException(
                    "No tienes permisos para actualizar materias."
            );
        }

        validarClaveDuplicada(
                request.clave(),
                carrera.getId(),
                id
        );

        materia.setClave(request.clave().trim());
        materia.setNombre(request.nombre().trim());
        materia.setSemestre(request.semestre());
        materia.setHorasSemana(request.horasSemana());
        materia.setCarrera(carrera);

        materia = materiaRepository.save(materia);

        return mapToResponse(materia);
    }

    // ==========================================
    // DESACTIVAR MATERIA
    // ==========================================

    @Override
    public void eliminar(Long id) {

        Usuario usuario = usuarioAutenticado.obtenerUsuario();

        String rol = usuario.getRol().name();

        Materia materia = materiaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Materia no encontrada."
                ));

        if ("ADMINISTRADOR".equals(rol)) {

            // Puede desactivar materias de cualquier carrera.

        } else if ("COORDINADOR".equals(rol)) {

            // Solo puede desactivar materias de su carrera.
            validarCarreraDelCoordinador(
                    materia,
                    usuario
            );

        } else {

            throw new AccessDeniedException(
                    "No tienes permisos para desactivar materias."
            );
        }

        materia.setActivo(false);

        materiaRepository.save(materia);
    }



    // ==========================================
    // REACTIVAR MATERIA
    // ==========================================

    @Override
    public void activar(Long id) {

        Usuario usuario = usuarioAutenticado.obtenerUsuario();

        String rol = usuario.getRol().name();

        Materia materia = materiaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Materia no encontrada."
                ));

        if ("ADMINISTRADOR".equals(rol)) {

            // El administrador puede activar materias
            // de cualquier carrera.

        } else if ("COORDINADOR".equals(rol)) {

            // El coordinador solamente puede activar
            // materias pertenecientes a su carrera.

            validarCarreraDelCoordinador(
                    materia,
                    usuario
            );

        } else {

            throw new AccessDeniedException(
                    "No tienes permisos para activar materias."
            );
        }

        materia.setActivo(true);

        materiaRepository.save(materia);
    }

    // ==========================================
    // OBTENER CARRERA DEL USUARIO
    // ==========================================

    private Carrera obtenerCarreraDelUsuario(Usuario usuario) {

        if (usuario.getCarrera() == null) {

            throw new AccessDeniedException(
                    "El usuario no tiene una carrera asignada."
            );
        }

        return usuario.getCarrera();
    }

    // ==========================================
    // VALIDAR CARRERA DEL COORDINADOR
    // ==========================================

    private void validarCarreraDelCoordinador(
            Materia materia,
            Usuario usuario
    ) {

        Carrera carreraUsuario = obtenerCarreraDelUsuario(usuario);

        if (
                materia.getCarrera() == null ||
                !materia.getCarrera().getId()
                        .equals(carreraUsuario.getId())
        ) {

            throw new AccessDeniedException(
                    "No puedes administrar materias de otra carrera."
            );
        }
    }

    // ==========================================
    // BUSCAR CARRERA
    // ==========================================

    private Carrera buscarCarrera(Long carreraId) {

        if (carreraId == null) {

            throw new IllegalArgumentException(
                    "Debes seleccionar una carrera."
            );
        }

        return carreraRepository.findById(carreraId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Carrera no encontrada."
                ));
    }

    // ==========================================
    // VALIDAR CLAVE DUPLICADA
    // ==========================================

    private void validarClaveDuplicada(
            String clave,
            Long carreraId,
            Long materiaId
    ) {

        String claveNormalizada = clave.trim();

        boolean existe;

        if (materiaId == null) {

            existe = materiaRepository.existsByClaveAndCarreraId(
                    claveNormalizada,
                    carreraId
            );

        } else {

            existe = materiaRepository
                    .existsByClaveAndCarreraIdAndIdNot(
                            claveNormalizada,
                            carreraId,
                            materiaId
                    );
        }

        if (existe) {

            throw new IllegalArgumentException(
                    "Ya existe una materia con esa clave en la carrera seleccionada."
            );
        }
    }

    // ==========================================
    // CONVERTIR A RESPONSE
    // ==========================================

    private MateriaResponse mapToResponse(Materia materia) {

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
