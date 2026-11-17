
package sigelab_api.usuario.repository;

import sigelab_api.usuario.entity.Usuario;
import sigelab_api.usuario.entity.Rol;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Buscar usuario por correo electrónico.
     */
    Optional<Usuario> findByCorreo(String correo);

    /**
     * Verificar si ya existe un usuario con ese correo.
     */
    boolean existsByCorreo(String correo);

    /**
     * Contar usuarios por rol.
     */
    long countByRol(Rol rol);

    /**
     * Obtener todos los usuarios incluyendo su carrera.
     * Evita LazyInitializationException al convertir las entidades
     * en objetos UsuarioResponse.
     */
    @Override
    @EntityGraph(attributePaths = {"carrera"})
    List<Usuario> findAll();
}
