package sigelab_api.usuario.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import sigelab_api.usuario.entity.Usuario;
import sigelab_api.usuario.repository.UsuarioRepository;

@Component
@RequiredArgsConstructor
public class UsuarioAutenticado {

    private final UsuarioRepository usuarioRepository;

    /**
     * Obtiene al usuario que tiene la sesión JWT activa.
     */
    public Usuario obtenerUsuario() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            throw new AuthenticationCredentialsNotFoundException(
                    "No existe un usuario autenticado."
            );
        }

        String correo = authentication.getName();

        Usuario usuario = usuarioRepository
                .findWithCarreraByCorreo(correo)
                .orElseThrow(() -> new AccessDeniedException(
                        "El usuario autenticado ya no existe."
                ));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new AccessDeniedException(
                    "El usuario se encuentra inactivo."
            );
        }

        return usuario;
    }

    /**
     * Obtiene el identificador de la carrera del usuario.
     */
    public Long obtenerCarreraId() {

        Usuario usuario = obtenerUsuario();

        if (usuario.getCarrera() == null) {
            throw new AccessDeniedException(
                    "El usuario no tiene una carrera asignada."
            );
        }

        return usuario.getCarrera().getId();
    }

    public boolean esAdministrador() {
        return obtenerUsuario().getRol().name()
                .equals("ADMINISTRADOR");
    }

    public boolean esCoordinador() {
        return obtenerUsuario().getRol().name()
                .equals("COORDINADOR");
    }

    public boolean esDocente() {
        return obtenerUsuario().getRol().name()
                .equals("DOCENTE");
    }

    public boolean esTecnico() {
        return obtenerUsuario().getRol().name()
                .equals("TECNICO");
    }
}