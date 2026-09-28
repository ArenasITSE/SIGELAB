package sigelab_api.usuario.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import sigelab_api.usuario.dto.UsuarioRequest;
import sigelab_api.usuario.dto.UsuarioResponse;
import sigelab_api.usuario.entity.Usuario;
import sigelab_api.usuario.repository.UsuarioRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl
        implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UsuarioResponse crear(
            UsuarioRequest request
    ) {

        if (usuarioRepository.existsByCorreo(
                request.correo()
        )) {
            throw new RuntimeException(
                    "Ya existe un usuario con ese correo"
            );
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre())
                .correo(request.correo())
                .password(
                        passwordEncoder.encode(
                                request.password()
                        )
                )
                .rol(request.rol())
                .activo(true)
                .build();

        usuario = usuarioRepository.save(usuario);

        return mapToResponse(usuario);
    }

    @Override
    public List<UsuarioResponse> listar() {

        return usuarioRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public UsuarioResponse buscarPorId(
            Long id
    ) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuario no encontrado"
                        ));

        return mapToResponse(usuario);
    }

    @Override
    public UsuarioResponse actualizar(
            Long id,
            UsuarioRequest request
    ) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuario no encontrado"
                        ));

        usuario.setNombre(request.nombre());
        usuario.setCorreo(request.correo());
        usuario.setRol(request.rol());

        usuario = usuarioRepository.save(usuario);

        return mapToResponse(usuario);
    }

    @Override
    public void eliminar(
            Long id
    ) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuario no encontrado"
                        ));

        usuarioRepository.delete(usuario);
    }

    @Override
    public UsuarioResponse activar(
            Long id
    ) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuario no encontrado"
                        ));

        usuario.setActivo(true);

        usuario = usuarioRepository.save(usuario);

        return mapToResponse(usuario);
    }

    @Override
    public UsuarioResponse desactivar(
            Long id
    ) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuario no encontrado"
                        ));

        usuario.setActivo(false);

        usuario = usuarioRepository.save(usuario);

        return mapToResponse(usuario);
    }

    private UsuarioResponse mapToResponse(
            Usuario usuario
    ) {

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol().name(),
                usuario.getActivo()
        );
    }
}