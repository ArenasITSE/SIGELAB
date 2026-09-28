package sigelab_api.usuario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sigelab_api.usuario.entity.Rol;
import sigelab_api.usuario.entity.Usuario;

import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    long countByRol(Rol rol);
}