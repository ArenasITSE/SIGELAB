
package sigelab_api.materia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sigelab_api.materia.entity.Materia;

import java.util.List;

@Repository
public interface MateriaRepository
        extends JpaRepository<Materia, Long> {

    List<Materia> findByCarreraId(Long carreraId);

    List<Materia> findByCarreraIdAndActivoTrue(Long carreraId);

    boolean existsByClaveAndCarreraId(
            String clave,
            Long carreraId
    );

    boolean existsByClaveAndCarreraIdAndIdNot(
            String clave,
            Long carreraId,
            Long id
    );
}
