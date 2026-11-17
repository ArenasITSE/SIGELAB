
package sigelab_api.security.service;

import lombok.RequiredArgsConstructor;

import sigelab_api.usuario.entity.Usuario;
import sigelab_api.usuario.repository.UsuarioRepository;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository
                .findByCorreo(correo)
                .orElseThrow(() ->
                    new UsernameNotFoundException(
                        "Usuario no encontrado: " + correo
                    )
                );

        // Validar que el usuario tenga un rol asignado
        if (usuario.getRol() == null) {
            throw new UsernameNotFoundException(
                "El usuario no tiene un rol asignado."
            );
        }

        // Obtener autoridad del usuario
        String autoridad = "ROLE_" + usuario.getRol().name();

System.out.println("Usuario autenticado: " + usuario.getCorreo());
System.out.println("Autoridad asignada: " + autoridad);
System.out.println("Usuario activo: " + usuario.getActivo());

        // Construir UserDetails
        return User.builder()
                .username(usuario.getCorreo())
                .password(usuario.getPassword())
                .authorities(
                    List.of(new SimpleGrantedAuthority(autoridad))
                )
                .disabled(!Boolean.TRUE.equals(usuario.getActivo()))
                .build();
    }
}
