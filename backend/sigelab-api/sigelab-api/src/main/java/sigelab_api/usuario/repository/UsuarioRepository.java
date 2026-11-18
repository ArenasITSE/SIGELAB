
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

    Optional<Usuario> findByCorreo(String correo);

    @EntityGraph(attributePaths = {"carrera"})
    Optional<Usuario> findWithCarreraByCorreo(String correo);

    boolean existsByCorreo(String correo);

    long countByRol(Rol rol);

    /**
     * Buscar usuario por ID incluyendo su carrera.
     */
    @EntityGraph(attributePaths = {"carrera"})
    Optional<Usuario> findWithCarreraById(Long id);

    /**
     * Listar usuarios de una carrera.
     */
    @EntityGraph(attributePaths = {"carrera"})
    List<Usuario> findAllByCarrera_Id(Long carreraId);

    /**
     * Buscar usuario por ID y carrera.
     */
    @EntityGraph(attributePaths = {"carrera"})
    Optional<Usuario> findByIdAndCarrera_Id(Long id, Long carreraId);

    /**
     * Obtener todos los usuarios incluyendo su carrera.
     */
    @Override
    @EntityGraph(attributePaths = {"carrera"})
    List<Usuario> findAll();
}
