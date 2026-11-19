
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

    @EntityGraph(attributePaths = {"carrera", "carreras"})
    Optional<Usuario> findWithCarreraByCorreo(String correo);

    boolean existsByCorreo(String correo);

    long countByRol(Rol rol);

    @EntityGraph(attributePaths = {"carrera", "carreras"})
    Optional<Usuario> findWithCarreraById(Long id);

    @EntityGraph(attributePaths = {"carrera", "carreras"})
    List<Usuario> findAllByCarrera_Id(Long carreraId);

    @EntityGraph(attributePaths = {"carrera", "carreras"})
    Optional<Usuario> findByIdAndCarrera_Id(Long id, Long carreraId);

    @Override
    @EntityGraph(attributePaths = {"carrera", "carreras"})
    List<Usuario> findAll();
}
