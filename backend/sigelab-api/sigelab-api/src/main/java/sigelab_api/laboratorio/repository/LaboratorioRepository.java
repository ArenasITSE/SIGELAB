
package sigelab_api.laboratorio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sigelab_api.laboratorio.entity.Laboratorio;

import java.util.List;
import java.util.Optional;

@Repository
public interface LaboratorioRepository
        extends JpaRepository<Laboratorio, Long> {

    /**
     * Obtener laboratorios pertenecientes a una carrera.
     */
    List<Laboratorio> findAllByCarrera_Id(Long carreraId);

    /**
     * Buscar un laboratorio por ID y carrera.
     */
    Optional<Laboratorio> findByIdAndCarrera_Id(
            Long id,
            Long carreraId
    );
}
